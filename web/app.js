// Remote Compose Storybook: the page shell.
//
// The catalog, the stories and the player live in Kotlin/Wasm (storybook.js), which installs
// `window.rcStorybook` and reports back through window events:
//   rcstorybook:ready     the bridge is installed
//   rcstorybook:document  a story produced a document  {story, bytes, operations, structure, diff, base64}
//   rcstorybook:event     the player raised an event    {story, type, detail}
//   rcstorybook:failure   a story failed to compose      {story, message}
// Everything else here (navigation, controls, code, docs) is plain DOM.
(function () {
  "use strict";

  const $ = (id) => document.getElementById(id);
  const H = window.RcHighlight;
  const esc = H.escape;

  const state = {
    catalog: null,
    sources: {},
    api: {},
    coverage: null,
    meta: {},
    components: [],
    storiesById: new Map(),
    componentByStory: new Map(),
    current: null, // { story, component, values }
    lastDocument: null,
    actions: [],
    expanded: new Set(),
    tab: "controls",
    documentShownFor: null,
  };

  // ------------------------------------------------------------------------------------------
  // Boot

  const ready = new Promise((resolve) => {
    if (window.rcStorybook) resolve();
    else window.addEventListener("rcstorybook:ready", () => resolve(), { once: true });
  });

  async function fetchJson(path, fallback) {
    try {
      const res = await fetch(path, { cache: "no-cache" });
      if (!res.ok) throw new Error(res.status + " " + path);
      return await res.json();
    } catch (e) {
      console.warn("Could not load", path, e);
      return fallback;
    }
  }

  async function boot() {
    initTheme();
    const [sources, api, coverage, meta] = await Promise.all([
      fetchJson("data/sources.json", {}),
      fetchJson("data/api.json", {}),
      fetchJson("data/coverage.json", null),
      fetchJson("data/meta.json", {}),
    ]);
    Object.assign(state, { sources, api, coverage, meta });
    if (meta.repository) $("repoLink").href = meta.repository;
    if (meta.androidxCommit) {
      $("versionBadge").textContent = "androidx-main@" + meta.androidxCommit.slice(0, 9);
    }
    await ready;
    state.catalog = JSON.parse(window.rcStorybook.catalog());
    state.components = state.catalog.components;
    for (const c of state.components) {
      for (const s of c.stories) {
        state.storiesById.set(s.id, s);
        state.componentByStory.set(s.id, c);
      }
    }
    $("playerTheme").value = effectiveDark() ? "dark" : "light";
    wireEvents();
    renderNav();
    route();
  }

  // ------------------------------------------------------------------------------------------
  // Theme

  function initTheme() {
    let saved = null;
    try {
      saved = localStorage.getItem("rcsb-theme");
    } catch {
      // Storage can be blocked (private windows, previews); the theme then follows the system.
    }
    if (saved) document.documentElement.dataset.theme = saved;
    $("themeBtn").addEventListener("click", () => {
      const dark = effectiveDark();
      const next = dark ? "light" : "dark";
      document.documentElement.dataset.theme = next;
      try {
        localStorage.setItem("rcsb-theme", next);
      } catch {
        // Not remembered when storage is blocked; the toggle still applies to this page.
      }
      $("playerTheme").value = next;
      if (state.current) {
        sizeStage();
        sendStage();
      }
    });
  }

  function effectiveDark() {
    const t = document.documentElement.dataset.theme;
    if (t) return t === "dark";
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  }

  // ------------------------------------------------------------------------------------------
  // Navigation

  const CATEGORY_ORDER = [
    "Layout",
    "Content",
    "Canvas",
    "Modifiers",
    "Interaction",
    "Animation",
    "State & expressions",
    "Styling",
  ];

  function renderNav(filter) {
    const q = (filter || "").trim().toLowerCase();
    const nav = $("nav");
    const byCategory = new Map();
    for (const c of state.components) {
      const matches =
        !q ||
        c.title.toLowerCase().includes(q) ||
        c.stories.some((s) => s.name.toLowerCase().includes(q) || s.function.toLowerCase().includes(q));
      if (!matches) continue;
      if (!byCategory.has(c.category)) byCategory.set(c.category, []);
      byCategory.get(c.category).push(c);
    }
    const cats = [...byCategory.keys()].sort((a, b) => {
      const ia = CATEGORY_ORDER.indexOf(a),
        ib = CATEGORY_ORDER.indexOf(b);
      return (ia < 0 ? 99 : ia) - (ib < 0 ? 99 : ib);
    });
    const currentId = state.current && state.current.story.id;
    let html = "";
    if (!q) {
      html += `<div class="nav-section">
        <a class="nav-link nav-top" href="#/" ${location.hash === "" || location.hash === "#/" ? 'aria-current="page"' : ""}>Overview</a>
        <a class="nav-link nav-top" href="#/coverage" ${location.hash === "#/coverage" ? 'aria-current="page"' : ""}>API coverage</a>
      </div>`;
    }
    for (const cat of cats) {
      html += `<div class="nav-section"><div class="nav-heading">${esc(cat)}</div>`;
      for (const c of byCategory.get(cat)) {
        const open = q || state.expanded.has(c.id) || (state.current && state.current.component.id === c.id);
        html += `<button class="nav-comp" data-comp="${c.id}" aria-expanded="${open ? "true" : "false"}">${esc(c.title)}</button>`;
        if (open) {
          for (const s of c.stories) {
            if (
              q &&
              !(
                c.title.toLowerCase().includes(q) ||
                s.name.toLowerCase().includes(q) ||
                s.function.toLowerCase().includes(q)
              )
            )
              continue;
            html += `<a class="nav-link" href="#/story/${s.id}" ${s.id === currentId ? 'aria-current="page"' : ""}>${esc(s.name)}</a>`;
          }
        }
      }
      html += `</div>`;
    }
    if (!cats.length) html += `<div class="nav-empty">No component matches “${esc(q)}”.</div>`;
    nav.innerHTML = html;
  }

  // ------------------------------------------------------------------------------------------
  // Routing

  function parseHash() {
    const raw = location.hash.replace(/^#/, "") || "/";
    const [path, query] = raw.split("?");
    return { path, params: new URLSearchParams(query || "") };
  }

  function route() {
    document.body.classList.remove("nav-open");
    const { path, params } = parseHash();
    const m = path.match(/^\/story\/([\w-]+)$/);
    if (m && state.storiesById.has(m[1])) {
      showStory(m[1], params);
    } else if (path === "/coverage") {
      showPage(renderCoverage());
    } else {
      showPage(renderOverview());
    }
    renderNav($("search").value);
  }

  function showPage(html) {
    state.current = null;
    $("page").innerHTML = html;
    $("page").hidden = false;
    $("storyView").hidden = true;
    $("stageWrap").classList.add("offstage");
    $("panel").hidden = true;
    $("main").scrollTop = 0;
    document.title = "Remote Compose Storybook";
  }

  // ------------------------------------------------------------------------------------------
  // Stories

  function defaultValues(story) {
    const v = {};
    for (const a of story.args) v[a.name] = a.default;
    return v;
  }

  function showStory(id, params) {
    const story = state.storiesById.get(id);
    const component = state.componentByStory.get(id);
    const switching = !state.current || state.current.story.id !== id;
    const raw = defaultValues(story);
    for (const a of story.args) {
      if (params.has(a.name)) raw[a.name] = params.get(a.name);
    }
    state.current = { story, component, values: raw };
    state.expanded.add(component.id);
    if (switching) {
      state.lastDocument = null;
      state.actions = [];
      updateActionsBadge();
      $("changesDot").hidden = true;
      $("docStats").textContent = "";
      $("downloadRc").disabled = true;
      overlay("Rendering…");
    }
    $("page").hidden = true;
    $("storyView").hidden = false;
    $("stageWrap").classList.remove("offstage");
    $("panel").hidden = false;
    $("crumbs").textContent = component.category + " / " + component.title;
    $("storyTitle").textContent = story.name;
    $("storyDesc").textContent = story.description;
    const plain = component.plain;
    $("storyPlain").innerHTML = plain
      ? `<div><b>What it is</b>${esc(plain.what)}</div>
      <div><b>Use it for</b><ul>${plain.useWhen.map((u) => `<li>${esc(u)}</li>`).join("")}</ul></div>`
      : "";
    document.title = component.title + " · " + story.name + " — Remote Compose Storybook";
    sizeStage();
    // Stage and story go to the player together, so it never re-renders the previous story at the
    // new size; the values come back canonical (clamped, defaulted) for the controls and the code.
    window.rcStorybook.stage(stageConfig());
    state.current.values = JSON.parse(window.rcStorybook.show(id, raw, switching));
    renderControls();
    renderCode();
    renderDocs();
    renderDocument();
    renderChanges();
    renderHostValues();
    renderActions();
    if (switching) $("main").scrollTop = 0;
  }

  // Control changes are coalesced to one per frame: a dragged slider fires far more often.
  let pending = false;
  function sendToPlayer() {
    if (pending) return;
    pending = true;
    requestAnimationFrame(() => {
      pending = false;
      if (!state.current) return;
      const { story, values } = state.current;
      state.current.values = JSON.parse(window.rcStorybook.show(story.id, values, false));
      renderCode();
      syncUrl();
    });
  }

  function sendStage() {
    if (state.current) window.rcStorybook.stage(stageConfig());
  }

  function setValue(name, value) {
    state.current.values[name] = value;
    sendToPlayer();
  }

  let urlTimer = 0;
  function syncUrl() {
    clearTimeout(urlTimer);
    urlTimer = setTimeout(() => {
      if (!state.current) return;
      const { story, values } = state.current;
      const params = new URLSearchParams();
      for (const a of story.args) if (values[a.name] !== a.default) params.set(a.name, values[a.name]);
      const q = params.toString();
      const hash = "#/story/" + story.id + (q ? "?" + q : "");
      if (location.hash !== hash) history.replaceState(null, "", hash);
    }, 250);
  }

  function overlay(text, isError) {
    const el = $("stageOverlay");
    if (text == null) {
      el.hidden = true;
      return;
    }
    el.hidden = false;
    el.textContent = text;
    el.classList.toggle("error", !!isError);
  }

  // Viewport ---------------------------------------------------------------------------------

  function stageConfig() {
    const size = state.stageSize || { w: 360, h: 360 };
    return { theme: $("playerTheme").value, width: size.w, height: size.h, layout: $("layout").value };
  }

  function sizeStage() {
    if (!state.current) return;
    const v = $("viewport").value;
    let w, h;
    if (v === "auto") {
      w = state.current.story.width;
      h = state.current.story.height;
    } else if (v === "fill") {
      w = Math.max(200, $("stageWrap").clientWidth - 50);
      h = Math.max(240, Math.round(window.innerHeight * 0.5));
    } else {
      [w, h] = v.split("x").map(Number);
    }
    const max = Math.max(200, $("stageWrap").clientWidth - 50);
    w = Math.min(w, max);
    const frame = $("stageFrame");
    frame.style.width = w + "px";
    frame.style.height = h + "px";
    frame.style.background = $("playerTheme").value === "dark" ? "#121316" : "#ffffff";
    $("stageSize").textContent = `${w} × ${h} dp`;
    state.stageSize = { w, h };
  }

  // Controls ---------------------------------------------------------------------------------

  function renderControls() {
    const { story, values } = state.current;
    const body = $("controls");
    if (!story.args.length) {
      body.innerHTML = `<p class="empty">This story has no args. Interact with it on the stage.</p>`;
      return;
    }
    let rows = "";
    for (const a of story.args)
      rows += `<tr><td class="name">${esc(a.name)}</td><td>${controlHtml(a, values[a.name])}</td><td class="doc">${esc(a.doc || "")}</td></tr>`;
    body.innerHTML = `<table class="controls"><thead><tr><th>Name</th><th>Control</th><th>Description</th></tr></thead><tbody>${rows}</tbody></table>`;
    body.querySelectorAll("[data-arg]").forEach((el) => {
      const name = el.dataset.arg;
      const arg = story.args.find((a) => a.name === name);
      const handler = () => {
        let value;
        if (arg.type === "boolean") value = el.checked ? "true" : "false";
        else if (arg.type === "color") {
          const alphaEl = body.querySelector(`[data-alpha="${name}"]`);
          const alpha = Number(alphaEl.value).toString(16).toUpperCase().padStart(2, "0");
          const rgb = body.querySelector(`[data-arg="${name}"]`).value.slice(1).toUpperCase();
          value = "#" + alpha + rgb;
        } else value = el.value;
        const out = body.querySelector(`output[data-for="${name}"]`);
        if (out) out.textContent = displayValue(arg, value);
        setValue(name, value);
      };
      el.addEventListener("input", handler);
      el.addEventListener("change", handler);
      const alphaEl = body.querySelector(`[data-alpha="${name}"]`);
      if (alphaEl) {
        alphaEl.addEventListener("input", handler);
      }
    });
  }

  function displayValue(arg, v) {
    if (arg.type === "float") return Number(v).toFixed(stepDecimals(arg.step));
    if (arg.type === "color") return "#" + String(v).replace("#", "");
    return String(v);
  }

  function stepDecimals(step) {
    const s = String(step);
    return s.includes(".") ? Math.min(3, s.split(".")[1].length) : 0;
  }

  function controlHtml(a, v) {
    const n = esc(a.name);
    switch (a.type) {
      case "boolean":
        return `<label class="switch"><input type="checkbox" data-arg="${n}" aria-label="${n}" ${v === "true" ? "checked" : ""}><span></span></label>`;
      case "int":
      case "float":
        return `<div class="control"><input type="range" data-arg="${n}" aria-label="${n}" min="${a.min}" max="${a.max}" step="${a.step}" value="${esc(String(v))}"><output data-for="${n}">${esc(displayValue(a, v))}</output></div>`;
      case "text":
        return `<div class="control"><input type="text" data-arg="${n}" aria-label="${n}" value="${esc(String(v))}"></div>`;
      case "color": {
        // Canonical colours are #AARRGGBB; anything else (never expected) falls back to opaque black.
        const hex = /^#[0-9A-F]{8}$/i.test(String(v)) ? String(v).slice(1).toUpperCase() : "FF000000";
        const alpha = parseInt(hex.slice(0, 2), 16);
        return `<div class="control"><input type="color" data-arg="${n}" aria-label="${n} colour" value="#${hex.slice(2)}">
          <label class="tool">alpha <input type="range" data-alpha="${n}" aria-label="${n} alpha" min="0" max="255" value="${alpha}" style="width:110px"></label>
          <output data-for="${n}">#${esc(hex)}</output></div>`;
      }
      case "choice":
        return `<select data-arg="${n}" aria-label="${n}">${a.options.map((o) => `<option ${o.label === v ? "selected" : ""}>${esc(o.label)}</option>`).join("")}</select>`;
      default:
        return esc(String(v));
    }
  }

  // Code -------------------------------------------------------------------------------------

  function kotlinValue(a, v) {
    switch (a.type) {
      case "boolean":
        return v === "true" ? "true" : "false";
      case "int":
        return String(v);
      case "float":
        // Canonical floats are Kotlin's Float.toString() ("30.0", "0.25", "1.0E-4").
        return v + "f";
      case "text":
        return (
          '"' + String(v).replace(/\\/g, "\\\\").replace(/"/g, '\\"').replace(/\$/g, "\\$").replace(/\n/g, "\\n") + '"'
        );
      case "color":
        return "Color(0x" + String(v).slice(1).toUpperCase() + ")";
      case "choice": {
        const o = a.options.find((o) => o.label === v);
        return o ? o.code : String(v);
      }
    }
    return String(v);
  }

  function usageCode() {
    const { story, values } = state.current;
    if (!story.args.length) return story.function + "()";
    const lines = story.args.map((a) => `    ${a.name} = ${kotlinValue(a, values[a.name])},`);
    return story.function + "(\n" + lines.join("\n") + "\n)";
  }

  function codeBlock(title, code, lang) {
    const html = lang === "diff" ? H.diff(code) : lang === "plain" ? esc(code) : H.kotlin(code);
    return `${title ? `<div class="code-title">${esc(title)}</div>` : ""}<div class="code-block"><button class="btn copy" data-copy>Copy</button><pre><code>${html}</code></pre></div>`;
  }

  function renderCode() {
    if (!state.current) return;
    const { story } = state.current;
    const src = state.sources[story.function];
    const body = $("code");
    let html = codeBlock("Usage with the current args", usageCode());
    html += src
      ? codeBlock("Story source · " + src.file, src.code)
      : `<p class="empty">Source for ${esc(story.function)} is missing from data/sources.json.</p>`;
    body.innerHTML = html;
  }

  // Docs -------------------------------------------------------------------------------------

  function renderDocs() {
    const { component } = state.current;
    let html = `<div class="docs"><p>${esc(component.description)}</p>`;
    for (const sym of component.symbols) {
      const sigs = state.api[sym];
      const short = sym.replace(/^androidx\.compose\.remote\./, "");
      html += `<h3><code>${esc(short)}</code></h3>`;
      if (sigs && sigs.length) html += codeBlock("", sigs.join("\n"), "plain");
      else html += `<p class="empty">No signature found in AndroidX's api/current.txt.</p>`;
    }
    const otherStories = component.stories.map((s) => `<a href="#/story/${s.id}">${esc(s.name)}</a>`).join(" · ");
    html += `<h3>Stories</h3><p>${otherStories}</p>`;
    const pkgs = [...new Set(component.symbols.map((s) => s.replace(/\.[^.]+$/, "")))];
    html += `<h3>Reference</h3><p>${pkgs.map((p) => `<a href="https://developer.android.com/reference/kotlin/${p.replace(/\./g, "/")}/package-summary" target="_blank" rel="noopener"><code>${esc(p)}</code></a>`).join("<br>")}</p></div>`;
    $("docs").innerHTML = html;
  }

  // Document / Changes / Actions --------------------------------------------------------------

  function renderDocument() {
    const doc = state.lastDocument;
    const body = $("document");
    if (!doc) {
      body.innerHTML = `<p class="empty">Waiting for the first document…</p>`;
      return;
    }
    body.innerHTML =
      `<p>The <code>.rc</code> document this story captures to right now: <b>${doc.bytes.toLocaleString()} bytes</b>, ${doc.operations} operations. This is what a server would send to a player${$("layout").value === "centered" ? " (with the stage's centering box around the story; choose Layout: Top start to see the story alone)" : ""}.</p>` +
      codeBlock("Structure (one operation per line)", doc.structure, "plain");
  }

  function renderChanges() {
    const doc = state.lastDocument;
    const body = $("changes");
    if (!doc || !doc.diff) {
      body.innerHTML = `<p class="empty">Change a control: this tab shows what that change did to the document, operation by operation.</p>`;
      return;
    }
    body.innerHTML =
      `<p>What the last change did to the document (structural diff of the previous and the current <code>.rc</code>).</p>` +
      codeBlock("", doc.diff, "diff");
  }

  // Host values ------------------------------------------------------------------------------

  const NAMED_TYPES = { 0: "text", 1: "float", 2: "color", 3: "image", 4: "int", 5: "long", 6: "floats" };

  function hostInput(v, label) {
    const kind = NAMED_TYPES[v.type] || "unknown";
    const attrs = `data-host="${esc(v.name)}" data-type="${v.type}" aria-label="${label}"`;
    if (kind === "text") return `<input type="text" ${attrs} placeholder="the document's value">`;
    if (kind === "float" || kind === "int" || kind === "long") {
      const step = kind === "float" ? "0.1" : "1";
      return `<input type="number" ${attrs} step="${step}" placeholder="the document's value">`;
    }
    if (kind === "color") return `<input type="color" ${attrs} value="#3949ab">`;
    return `<span class="empty">${esc(kind)}: set by the host app in code</span>`;
  }

  function renderHostValues() {
    const body = $("host");
    const doc = state.lastDocument;
    const named = (doc && doc.named) || [];
    if (!named.length) {
      body.innerHTML = `<p class="empty">This story's document declares no named values. The stories under
        <a href="#/story/named-state--card">rememberNamedRemote*</a> do: a host app can change them while the
        document plays, without a new document from the server.</p>`;
      return;
    }
    const rows = named
      .map((v) => {
        const label = esc(v.name.replace(/^USER:/, ""));
        const kind = esc(NAMED_TYPES[v.type] || "unknown");
        return `<tr><td class="name">${label}</td><td><code>${kind}</code></td><td><div class="control">${hostInput(v, label)}</div></td></tr>`;
      })
      .join("");
    body.innerHTML = `<p>A host app can set these values by name while the document plays. Change one: the stage
      replays the <b>same ${doc.bytes.toLocaleString()}-byte document</b> with your value, with no new document from
      the server. That is how live data reaches a Remote Compose screen. Changing a control resets them, because that
      makes a new document.</p>
      <table class="controls"><thead><tr><th>Name</th><th>Type</th><th>Host value</th></tr></thead><tbody>${rows}</tbody></table>
      <p><button class="btn" id="hostReset">Back to the document's values</button></p>`;
    body.querySelectorAll("[data-host]").forEach((el) => {
      el.addEventListener("input", () => {
        const type = Number(el.dataset.type);
        const value = type === 2 ? "#FF" + el.value.slice(1).toUpperCase() : el.value;
        window.rcStorybook.hostValue(el.dataset.host, type, value);
        $("hostState").textContent = "same document, host values";
      });
    });
    $("hostReset").addEventListener("click", () => {
      window.rcStorybook.hostReset();
      renderHostValues();
      $("hostState").textContent = "";
    });
  }

  function updateActionsBadge() {
    const c = $("actionsCount");
    c.hidden = state.actions.length === 0;
    c.textContent = String(state.actions.length);
  }

  function renderActions() {
    const body = $("actions");
    if (!state.actions.length) {
      body.innerHTML = `<p class="empty">Events the player sends to its host (hostAction, haptics, …) appear here. Try a story under Interaction and tap the stage.</p>`;
      return;
    }
    body.innerHTML = `<ul class="action-log">${state.actions
      .slice()
      .reverse()
      .map((a) => `<li><time>${a.time}</time><b>${esc(a.type)}</b><span>${esc(a.detail)}</span></li>`)
      .join("")}</ul>
      <p><button class="btn" id="clearActions">Clear</button></p>`;
    $("clearActions").addEventListener("click", () => {
      state.actions = [];
      updateActionsBadge();
      renderActions();
    });
  }

  // Pages ------------------------------------------------------------------------------------

  function renderOverview() {
    const total = state.components.reduce((n, c) => n + c.stories.length, 0);
    const cov = state.coverage ? state.coverage.blocks : [];
    const covered = cov.filter((b) => b.stories.length || b.inOptions).length;
    const cats = [...new Set(state.components.map((c) => c.category))].sort(
      (a, b) => CATEGORY_ORDER.indexOf(a) - CATEGORY_ORDER.indexOf(b),
    );
    const first = (c) => (c.stories[0] ? "#/story/" + c.stories[0].id : "#/");
    return `
      <h1>Remote Compose Storybook</h1>
      <p class="lede">A live catalog of the public <b>Jetpack Remote Compose</b> creation API, the <code>@RemoteComposable</code>
      building blocks from <code>androidx.compose.remote</code>. Every story is composed in your browser, captured to a
      <code>.rc</code> document and drawn by a player, so you can see what each block does, how to call it, and how its
      document changes when its arguments change.</p>
      <p>${state.components.length} components · ${total} stories · ${covered}/${cov.length} public building blocks covered
      (<a href="#/coverage">API coverage</a>).</p>

      <h2>What is Remote Compose?</h2>
      <p>Remote Compose lets a server describe a piece of app UI as a small document instead of shipping new app code.
      The app plays the document: it lays it out, draws it, animates it and reacts to taps on the device, and the server can change
      it at any time without an app release. This site shows every building block such a document can be made of.</p>
      <div class="audience">
        <div><h3>Designers</h3><p>Browse the components, change their controls and see what is possible: layouts, text, images,
        shapes, gradients, animations and interactive states. Each component explains what it is and when to use it.</p></div>
        <div><h3>Product managers</h3><p>See which kinds of screens, widgets and interactions can be updated from the server without
        an app release. The <a href="#/coverage">API coverage</a> page lists every building block.</p></div>
        <div><h3>Developers</h3><p>Every story shows the exact Kotlin to write, the public signatures, the bytes the document becomes
        and how they change when an argument changes.</p></div>
      </div>

      <h2>How to read a story</h2>
      <p><b>Controls</b> change the story's arguments; the document is rebuilt on every change.
      <b>Code</b> shows the call with the current arguments and the story's source, written against
      AndroidX's packages and ready to paste. <b>Docs</b> lists the public signatures from AndroidX's <code>api/current.txt</code>.
      <b>Document</b> is the operation list a server would send; <b>Changes</b> is what your last change did to it;
      <b>Host values</b> lets you play the app's part: set a named value and the same document redraws with it, no new document; <b>Actions</b> logs the events the player sends to its host.</p>

      <h2>Use it in your project</h2>
      <p>The catalog follows <b>androidx-main</b>: the newest public API, a step ahead of the latest release
      (<code>${esc(state.meta.androidxRelease || "see Google Maven")}</code>). Use the next release, or AndroidX snapshots, to get every block shown here.</p>
      ${codeBlock(
        "build.gradle.kts",
        `repositories {
    google()
    // Snapshots of androidx-main: https://androidx.dev
    maven("https://androidx.dev/snapshots/latest/artifacts/repository")
}

dependencies {
    // Author documents (the API in this catalog)
    implementation("androidx.compose.remote:remote-creation-compose:1.0.0-SNAPSHOT")
    implementation("androidx.compose.remote.foundation:foundation:1.0.0-SNAPSHOT")
    // Play documents on Android
    implementation("androidx.compose.remote:remote-player-compose:1.0.0-SNAPSHOT")
}`,
      )}
      ${codeBlock(
        "Capture a @RemoteComposable to bytes (Android)",
        `val document = captureSingleRemoteDocument(
    context = context,
    creationDisplayInfo = RemoteCreationDisplayInfo(width = 1080, height = 1920, densityDpi = 420),
) {
    RemoteColumn(RemoteModifier.fillMaxSize().padding(16.rdp)) {
        RemoteText("Hello from the server".rs, fontSize = 20.rsp)
    }
}
// document.bytes is the .rc payload any Remote Compose player can draw.`,
      )}

      <h2>Components</h2>
      ${cats
        .map(
          (cat) =>
            `<h3>${esc(cat)}</h3><div class="cards">${state.components
              .filter((c) => c.category === cat)
              .map(
                (c) =>
                  `<a class="card" href="${first(c)}"><b>${esc(c.title)}</b><span>${esc(c.plain ? c.plain.what : c.description)}</span></a>`,
              )
              .join("")}</div>`,
        )
        .join("")}

      <h2>How this site works</h2>
      <p>The stories are compiled to WebAssembly with <a href="https://github.com/yschimke/rc-players" target="_blank" rel="noopener">rc-players</a>,
      a Compose Multiplatform port of AndroidX's creation API that differs from it only by package. Each story is composed live by
      <code>RcEmbeddedPlayer</code> and drawn by rc-players' Compose player. The source shown in the Code tab is rewritten to
      <code>androidx.compose.remote.*</code>, and CI compiles every story, exactly as shown, against AndroidX's own artifacts (an androidx.dev snapshot of androidx-main), so what you copy compiles.</p>`;
  }

  function renderCoverage() {
    if (!state.coverage) return `<h1>API coverage</h1><p class="empty">data/coverage.json is missing.</p>`;
    const blocks = state.coverage.blocks;
    const storyFor = new Map();
    for (const s of state.storiesById.values()) if (!storyFor.has(s.function)) storyFor.set(s.function, s);
    // Stories whose control options print this name in the Code tab.
    const viaOptions = (name) => {
      const member = name.split(".").pop();
      const re = new RegExp("(^|[^\\w])" + member.replace(/[.*+?^${}()|[\]\\]/g, "\\$&") + "(?!\\w)");
      const out = [];
      for (const s of state.storiesById.values()) {
        if (s.args.some((a) => a.options && a.options.some((o) => re.test(o.code)))) out.push(s);
      }
      return out;
    };
    const covered = blocks.filter((b) => b.stories.length || b.inOptions).length;
    const pending = blocks.filter((b) => b.pending && !b.stories.length).length;
    const groups = new Map();
    for (const b of blocks) {
      if (!groups.has(b.group)) groups.set(b.group, []);
      groups.get(b.group).push(b);
    }
    let html = `<h1>API coverage</h1>
      <p class="lede">Every public building block in AndroidX's <code>api/current.txt</code>
      (androidx-main <code>${esc((state.coverage.commit || "").slice(0, 9))}</code>) and the stories that use it.</p>
      <p><b>${covered}</b> of <b>${blocks.length}</b> covered${pending ? `; <b>${pending}</b> are public on androidx-main but not yet in the web player` : ""}.</p>
      <div class="meter"><div style="width:${((100 * covered) / Math.max(1, blocks.length)).toFixed(1)}%"></div></div>`;
    for (const [group, items] of groups) {
      const n = items.filter((b) => b.stories.length || b.inOptions).length;
      html += `<div class="cov-group"><h2>${esc(group)} <span class="pill">${n}/${items.length}</span></h2><table class="cov-table">`;
      for (const b of items) {
        const direct = b.stories.map((fn) => storyFor.get(fn)).filter(Boolean);
        const opts = b.inOptions ? viaOptions(b.name).filter((s) => !direct.includes(s)) : [];
        const links = [...direct, ...opts].map((s) => `<a href="#/story/${s.id}">${esc(s.name)}</a>`).join("");
        const note =
          b.pending && !links
            ? `<span class="pill">not in the web player yet</span> <span class="empty">${esc(b.pending)}</span>`
            : '<span class="empty">no story yet</span>';
        html += `<tr><td>${esc(b.name)}</td><td>${links || note}</td></tr>`;
      }
      html += `</table></div>`;
    }
    return html;
  }

  // ------------------------------------------------------------------------------------------
  // Events

  function toast(text) {
    const t = $("toast");
    t.textContent = text;
    t.classList.add("show");
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.classList.remove("show"), 1600);
  }

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
      toast("Copied");
    } catch {
      toast("Copy failed");
    }
  }

  function selectTab(name) {
    state.tab = name;
    document.querySelectorAll(".tabs [role=tab]").forEach((b) => {
      const selected = b.dataset.tab === name;
      b.setAttribute("aria-selected", String(selected));
      b.tabIndex = selected ? 0 : -1;
    });
    document.querySelectorAll(".tab-body").forEach((b) => {
      b.hidden = b.dataset.body !== name;
    });
    if (name === "changes") $("changesDot").hidden = true;
  }

  function wireEvents() {
    window.addEventListener("hashchange", route);
    $("search").addEventListener("input", (e) => renderNav(e.target.value));
    document.addEventListener("keydown", (e) => {
      const typing = ["INPUT", "TEXTAREA", "SELECT"].includes(document.activeElement.tagName);
      if (e.key === "/" && !typing && !e.metaKey && !e.ctrlKey && !e.altKey) {
        e.preventDefault();
        $("search").focus();
      }
    });
    $("nav").addEventListener("click", (e) => {
      const btn = e.target.closest(".nav-comp");
      if (!btn) return;
      const id = btn.dataset.comp;
      const comp = state.components.find((c) => c.id === id);
      if (state.expanded.has(id) && !(state.current && state.current.component.id === id)) state.expanded.delete(id);
      else {
        state.expanded.add(id);
        if (comp && comp.stories[0] && !(state.current && state.current.component.id === id))
          location.hash = "#/story/" + comp.stories[0].id;
      }
      renderNav($("search").value);
    });
    $("menuBtn").addEventListener("click", () => document.body.classList.toggle("nav-open"));
    $("scrim").addEventListener("click", () => document.body.classList.remove("nav-open"));
    document.querySelector(".tabs").addEventListener("keydown", (e) => {
      const tabs = [...document.querySelectorAll(".tabs [role=tab]")];
      const i = tabs.indexOf(document.activeElement);
      if (i < 0) return;
      let next = -1;
      if (e.key === "ArrowRight") next = (i + 1) % tabs.length;
      else if (e.key === "ArrowLeft") next = (i - 1 + tabs.length) % tabs.length;
      else if (e.key === "Home") next = 0;
      else if (e.key === "End") next = tabs.length - 1;
      if (next < 0) return;
      e.preventDefault();
      selectTab(tabs[next].dataset.tab);
      tabs[next].focus();
    });
    document.querySelector(".tabs").addEventListener("click", (e) => {
      const b = e.target.closest("[role=tab]");
      if (b) selectTab(b.dataset.tab);
    });
    document.addEventListener("click", (e) => {
      const b = e.target.closest("[data-copy]");
      if (b) copyText(b.parentElement.querySelector("pre").innerText);
    });
    const restage = () => {
      if (!state.current) return;
      sizeStage();
      sendStage();
    };
    $("viewport").addEventListener("change", restage);
    $("playerTheme").addEventListener("change", restage);
    $("layout").addEventListener("change", restage);
    let resizeTimer = 0;
    window.addEventListener("resize", () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(restage, 150);
    });
    $("resetArgs").addEventListener("click", () => {
      if (!state.current) return;
      const { story } = state.current;
      state.current.values = JSON.parse(window.rcStorybook.show(story.id, defaultValues(story), false));
      renderControls();
      renderCode();
      syncUrl();
    });
    $("copyLink").addEventListener("click", () => {
      syncUrl();
      setTimeout(() => copyText(location.href), 300);
    });
    $("downloadRc").addEventListener("click", () => {
      const doc = state.lastDocument;
      if (!doc) return;
      const bin = atob(doc.base64);
      const bytes = new Uint8Array(bin.length);
      for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
      const a = document.createElement("a");
      a.href = URL.createObjectURL(new Blob([bytes], { type: "application/octet-stream" }));
      a.download = state.current.story.id + ".rc";
      a.click();
      setTimeout(() => URL.revokeObjectURL(a.href), 1000);
    });

    window.addEventListener("rcstorybook:document", (e) => {
      const doc = JSON.parse(e.detail);
      if (!state.current || doc.story !== state.current.story.id) return;
      state.lastDocument = doc;
      overlay(null);
      $("docStats").textContent = `${doc.bytes.toLocaleString()} bytes · ${doc.operations} ops`;
      $("downloadRc").disabled = false;
      renderDocument();
      renderChanges();
      renderHostValues();
      $("hostState").textContent = "";
      if (doc.diff && state.tab !== "changes") $("changesDot").hidden = false;
      document.body.dataset.rendered = doc.story;
    });
    window.addEventListener("rcstorybook:event", (e) => {
      const ev = JSON.parse(e.detail);
      if (!state.current || ev.story !== state.current.story.id) return;
      state.actions.push({ time: new Date().toLocaleTimeString(), type: ev.type, detail: ev.detail });
      if (state.actions.length > 200) state.actions.shift();
      updateActionsBadge();
      renderActions();
    });
    window.addEventListener("rcstorybook:failure", (e) => {
      const f = JSON.parse(e.detail);
      if (!state.current || f.story !== state.current.story.id) return;
      overlay("This story failed to compose:\n" + f.message, true);
      document.body.dataset.failed = f.story;
    });
  }

  // A crash inside the Wasm player leaves its canvas dead: say so rather than showing a blank stage.
  window.addEventListener("error", (e) => {
    // Only errors from the Wasm bundle; the shell's own errors are not the player's.
    if (!state.current || !/storybook\.js|\.wasm/.test(e.filename || "")) return;
    overlay("The player stopped: " + (e.message || "unknown error") + "\nReload the page to continue.", true);
  });

  boot();
})();
