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
  const I18n = window.RcI18n;
  const t = I18n.t;
  const esc = H.escape;

  // ------------------------------------------------------------------------------------------
  // HTML: escaped by default
  //
  // Markup is built with the html`` tag, which escapes every interpolation unless it is already
  // SafeHtml (another html`` result, or raw()). Arrays are joined, each item treated the same way;
  // null and undefined become "". setHtml() is the only way markup reaches the DOM and accepts
  // nothing but SafeHtml, so a plain string is always escaped. raw() is the only way around it, and
  // each raw() call needs an eslint-disable with its reason (no-restricted-syntax in e2e/eslint.config.mjs).
  // Whitespace inside html`` is part of the page, so Prettier leaves these templates alone
  // (embeddedLanguageFormatting is off for web/ in .prettierrc.json).

  class SafeHtml {
    constructor(markup) {
      this.markup = markup;
      Object.freeze(this);
    }
  }

  function htmlPart(value) {
    if (value instanceof SafeHtml) return value.markup;
    if (value == null) return "";
    if (Array.isArray(value)) return value.map(htmlPart).join("");
    return esc(value);
  }

  function html(strings, ...values) {
    let out = strings[0];
    for (let i = 0; i < values.length; i++) out += htmlPart(values[i]) + strings[i + 1];
    return new SafeHtml(out);
  }

  // Trusted markup only: bundled translations that hold markup (tHtml) and highlight.js output,
  // which escapes the source text itself. Never data from the catalog, the URL or the player.
  const raw = (markup) => new SafeHtml(String(markup));

  // Items joined by `separator` (a plain string is escaped, like any interpolation).
  const joinHtml = (items, separator) => new SafeHtml(items.map(htmlPart).join(htmlPart(separator)));

  // A translation whose text holds markup (i18n.js: bundled, never user data). Its {vars} are
  // escaped like any interpolation. Plain-text translations go through t() and html`` instead.
  function tHtml(key, vars) {
    const escaped = {};
    for (const [k, v] of Object.entries(vars || {})) escaped[k] = htmlPart(v);
    // eslint-disable-next-line no-restricted-syntax -- a bundled translation; its {vars} are escaped above
    return raw(t(key, escaped));
  }

  function setHtml(element, value) {
    if (!(value instanceof SafeHtml)) throw new TypeError("setHtml() takes SafeHtml from html`` or raw()");
    // eslint-disable-next-line no-unsanitized/property -- the page's only HTML sink: value is SafeHtml, built by html`` (escapes every interpolation) or raw() (bundled translations, highlight.js output)
    element.innerHTML = value.markup;
  }

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

  // The player is several MB of WebAssembly. Download it here, where progress can be shown, then add
  // storybook.js; its fetches of the same files are answered from what was downloaded, so nothing
  // is fetched twice whatever the server's cache headers say.
  const preloaded = new Map();
  const realFetch = window.fetch.bind(window);
  window.fetch = (input, init) => {
    const url = typeof input === "string" ? input : input && input.url;
    if (url) {
      for (const [file, blob] of preloaded) {
        if (url.split("?")[0].endsWith("/" + file) || url === file) {
          return Promise.resolve(new Response(blob, { headers: { "content-type": "application/wasm" } }));
        }
      }
    }
    return realFetch(input, init);
  };

  async function loadPlayer() {
    const files = (await fetchJson("data/assets.json", { wasm: [] })).wasm;
    const total = files.reduce((n, f) => n + f.bytes, 0);
    let done = 0;
    const show = () => {
      const pct = total ? Math.round((100 * done) / total) : 0;
      $("loadingBar").style.width = pct + "%";
      $("loadingDetail").textContent = t("loading.progress", {
        done: (done / 1e6).toFixed(1),
        total: (total / 1e6).toFixed(1),
        pct,
      });
    };
    show();
    await Promise.all(
      files.map(async (f) => {
        try {
          const res = await realFetch(f.path);
          const reader = res.body.getReader();
          const chunks = [];
          for (;;) {
            const { done: end, value } = await reader.read();
            if (end) break;
            chunks.push(value);
            done += value.length;
            show();
          }
          preloaded.set(f.path, new Blob(chunks, { type: "application/wasm" }));
        } catch (e) {
          console.warn("Could not preload", f.path, e);
        }
      }),
    );
    $("loadingDetail").textContent = t("loading.starting");
    const script = document.createElement("script");
    script.src = "storybook.js";
    script.onerror = () => {
      $("loadingDetail").textContent = t("loading.failed");
    };
    document.body.appendChild(script);
  }

  // Compose Multiplatform gives its canvas role="generic", which is not a valid role for a canvas.
  // It is an interactive surface (taps, drags, keys), so describe it as one.
  function labelPlayerCanvas() {
    // The canvas lives in a shadow root that Compose attaches inside #stage.
    const findCanvas = () => {
      for (const el of [$("stage"), ...$("stage").querySelectorAll("*")]) {
        const canvas = (el.shadowRoot || el).querySelector("canvas");
        if (canvas) return canvas;
      }
      return null;
    };
    let observed = null;
    const label = () => {
      const canvas = findCanvas();
      if (!canvas) return;
      if (canvas.getAttribute("role") !== "application") {
        canvas.setAttribute("role", "application");
        canvas.setAttribute("aria-label", "Remote Compose player: interact with the story");
      }
      if (observed !== canvas) {
        observed = canvas;
        // Compose sets the role again as it updates its canvas: keep it valid.
        new MutationObserver(label).observe(canvas, { attributes: true, attributeFilter: ["role"] });
      }
    };
    label();
    new MutationObserver(label).observe($("stage"), { childList: true, subtree: true });
  }

  function initLanguage() {
    I18n.apply(document);
    $("langBtn").addEventListener("click", () => {
      I18n.set(I18n.lang === "ru" ? "en" : "ru");
      I18n.apply(document);
      if (state.catalog) {
        const current = state.current;
        if (current) state.current = null; // re-render the story view in the new language
        route();
      }
    });
  }

  async function boot() {
    initTheme();
    initLanguage();
    loadPlayer();
    const [sources, api, coverage, meta] = await Promise.all([
      fetchJson("data/sources.json", {}),
      fetchJson("data/api.json", {}),
      fetchJson("data/coverage.json", null),
      fetchJson("data/meta.json", {}),
    ]);
    Object.assign(state, { sources, api, coverage, meta });
    if (meta.repository) $("repoLink").href = meta.repository;
    if (meta.androidxApi && meta.androidxApi["remote-creation-compose"]) {
      $("versionBadge").textContent = "AndroidX " + meta.androidxApi["remote-creation-compose"];
    } else if (meta.androidxCommit) {
      $("versionBadge").textContent = "androidx-main@" + meta.androidxCommit.slice(0, 9);
    }
    await ready;
    $("loading").hidden = true;
    labelPlayerCanvas();
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
    "Recipes",
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
    const current = html`aria-current="page"`;
    const parts = [];
    if (!q) {
      parts.push(html`<div class="nav-section">
        <a class="nav-link nav-top" href="#/" ${location.hash === "" || location.hash === "#/" ? current : ""}>${t("nav.overview")}</a>
        <a class="nav-link nav-top" href="#/coverage" ${location.hash === "#/coverage" ? current : ""}>${t("nav.coverage")}</a>
      </div>`);
    }
    for (const cat of cats) {
      parts.push(html`<div class="nav-section"><div class="nav-heading">${I18n.category(cat)}</div>`);
      for (const c of byCategory.get(cat)) {
        const open = q || state.expanded.has(c.id) || (state.current && state.current.component.id === c.id);
        parts.push(
          html`<button class="nav-comp" data-comp="${c.id}" aria-expanded="${open ? "true" : "false"}">${c.title}</button>`,
        );
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
            parts.push(
              html`<a class="nav-link" href="#/story/${s.id}" ${s.id === currentId ? current : ""}>${s.name}</a>`,
            );
          }
        }
      }
      parts.push(html`</div>`);
    }
    if (!cats.length) parts.push(html`<div class="nav-empty">${t("nav.empty", { q })}</div>`);
    setHtml(nav, html`${parts}`);
  }

  // ------------------------------------------------------------------------------------------
  // Routing

  function parseHash() {
    const hash = location.hash.replace(/^#/, "") || "/";
    const [path, query] = hash.split("?");
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

  function showPage(content) {
    state.current = null;
    setHtml($("page"), content);
    $("page").hidden = false;
    $("storyView").hidden = true;
    $("stageWrap").classList.add("offstage");
    $("panel").hidden = true;
    $("main").scrollTop = 0;
    document.title = "Remote Compose Storybook — live catalog of Jetpack Remote Compose components";
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
    const requested = defaultValues(story);
    for (const a of story.args) {
      if (params.has(a.name)) requested[a.name] = params.get(a.name);
    }
    state.current = { story, component, values: requested };
    state.expanded.add(component.id);
    if (switching) {
      state.lastDocument = null;
      state.actions = [];
      updateActionsBadge();
      $("changesDot").hidden = true;
      $("docStats").textContent = "";
      $("downloadRc").disabled = true;
      overlay(t("stage.rendering"));
    }
    $("page").hidden = true;
    $("storyView").hidden = false;
    $("stageWrap").classList.remove("offstage");
    $("panel").hidden = false;
    $("crumbs").textContent = I18n.category(component.category) + " / " + component.title;
    $("storyTitle").textContent = story.name;
    $("storyDesc").textContent = story.description;
    const plain = I18n.plain(component);
    setHtml(
      $("storyPlain"),
      plain
        ? html`<section class="plain-card"><h2><span class="plain-icon" aria-hidden="true">i</span>${t("plain.what")}</h2>
          <p>${plain.what}</p></section>
        <section class="plain-card"><h2><span class="plain-icon" aria-hidden="true">★</span>${t("plain.use")}</h2>
          <ul>${plain.useWhen.map((u) => html`<li>${u}</li>`)}</ul></section>`
        : html``,
    );
    const how = I18n.how(component);
    $("storyHow").hidden = !how;
    // `how` is the hand-written HTML for this component, bundled in i18n.js.
    // eslint-disable-next-line no-restricted-syntax -- how is I18n.how(): bundled markup from i18n.js
    setHtml($("storyHow"), how ? html`<summary>${t("how.title")}</summary>${raw(how)}` : html``);
    document.title = component.title + " · " + story.name + " — Remote Compose Storybook";
    sizeStage();
    // Stage and story go to the player together, so it never re-renders the previous story at the
    // new size; the values come back canonical (clamped, defaulted) for the controls and the code.
    window.rcStorybook.stage(stageConfig());
    state.current.values = JSON.parse(window.rcStorybook.show(id, requested, switching));
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

  // The link to hand out. A published build has a page per story (s/<id>/) that carries the
  // story's link preview; the "#/story/..." address cannot have one. That page sends a browser on
  // to the story only when the link has a query (the args, or "play"): without one it is the
  // story's static page for search engines.
  function shareUrl() {
    const { path, params } = parseHash();
    const m = path.match(/^\/story\/([\w-]+)$/);
    if (!state.meta.sharePages || !m) return location.href;
    const q = params.toString() || "play";
    return new URL("s/" + m[1] + "/?" + q, location.href.split("#")[0].split("?")[0]).href;
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
      setHtml(body, html`<p class="empty">${t("controls.none")}</p>`);
      return;
    }
    const rows = story.args.map(
      (a) =>
        html`<tr><td class="name">${a.name}</td><td>${controlHtml(a, values[a.name])}</td><td class="doc">${a.doc || ""}</td></tr>`,
    );
    setHtml(
      body,
      html`<table class="controls"><thead><tr><th>${t("controls.name")}</th><th>${t("controls.control")}</th><th>${t("controls.description")}</th></tr></thead><tbody>${rows}</tbody></table>`,
    );
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
    const n = a.name;
    switch (a.type) {
      case "boolean":
        return html`<label class="switch"><input type="checkbox" data-arg="${n}" aria-label="${n}" ${v === "true" ? "checked" : ""}><span></span></label>`;
      case "int":
      case "float":
        return html`<div class="control"><input type="range" data-arg="${n}" aria-label="${n}" min="${a.min}" max="${a.max}" step="${a.step}" value="${String(v)}"><output data-for="${n}">${displayValue(a, v)}</output></div>`;
      case "text":
        return html`<div class="control"><input type="text" data-arg="${n}" aria-label="${n}" value="${String(v)}"></div>`;
      case "color": {
        // Canonical colours are #AARRGGBB; anything else (never expected) falls back to opaque black.
        const hex = /^#[0-9A-F]{8}$/i.test(String(v)) ? String(v).slice(1).toUpperCase() : "FF000000";
        const alpha = parseInt(hex.slice(0, 2), 16);
        return html`<div class="control"><input type="color" data-arg="${n}" aria-label="${n} colour" value="#${hex.slice(2)}">
          <label class="tool">alpha <input type="range" data-alpha="${n}" aria-label="${n} alpha" min="0" max="255" value="${alpha}" style="width:110px"></label>
          <output data-for="${n}">#${hex}</output></div>`;
      }
      case "choice":
        return html`<select data-arg="${n}" aria-label="${n}">${a.options.map((o) => html`<option ${o.label === v ? "selected" : ""}>${o.label}</option>`)}</select>`;
      default:
        return html`${String(v)}`;
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
    // highlight.js escapes the source text itself and adds only its own <span> tags.
    // eslint-disable-next-line no-restricted-syntax -- highlight.js escapes every token it does not wrap in its own <span>
    const highlighted = lang === "diff" ? raw(H.diff(code)) : lang === "plain" ? code : raw(H.kotlin(code));
    return html`${title ? html`<div class="code-title">${title}</div>` : ""}<div class="code-block"><button class="btn copy" data-copy>${t("code.copy")}</button><pre><code>${highlighted}</code></pre></div>`;
  }

  function renderCode() {
    if (!state.current) return;
    const { story } = state.current;
    const src = state.sources[story.function];
    setHtml(
      $("code"),
      html`${codeBlock(t("code.usage"), usageCode())}${
        src
          ? codeBlock(t("code.source", { file: src.file }), src.code)
          : html`<p class="empty">${t("code.missing", { fn: story.function })}</p>`
      }`,
    );
  }

  // Docs -------------------------------------------------------------------------------------

  function renderDocs() {
    const { component } = state.current;
    const parts = [html`<div class="docs"><p>${component.description}</p>`];
    const how = I18n.how(component);
    // `how` is the hand-written HTML for this component, bundled in i18n.js.
    // eslint-disable-next-line no-restricted-syntax -- how is I18n.how(): bundled markup from i18n.js
    if (how) parts.push(html`<h3>${t("how.title")}</h3>${raw(how)}`);
    for (const sym of component.symbols) {
      const sigs = state.api[sym];
      const short = sym.replace(/^androidx\.compose\.remote\./, "");
      parts.push(html`<h3><code>${short}</code></h3>`);
      if (sigs && sigs.length) parts.push(codeBlock("", sigs.join("\n"), "plain"));
      else parts.push(html`<p class="empty">${t("docs.noSignature")}</p>`);
    }
    const otherStories = joinHtml(
      component.stories.map((s) => html`<a href="#/story/${s.id}">${s.name}</a>`),
      " · ",
    );
    parts.push(html`<h3>${t("docs.stories")}</h3><p>${otherStories}</p>`);
    const pkgs = [...new Set(component.symbols.map((s) => s.replace(/\.[^.]+$/, "")))];
    const refs = pkgs.map(
      (p) =>
        html`<a href="https://developer.android.com/reference/kotlin/${p.replace(/\./g, "/")}/package-summary" target="_blank" rel="noopener"><code>${p}</code></a>`,
    );
    parts.push(html`<h3>${t("docs.reference")}</h3><p>${joinHtml(refs, html`<br>`)}</p></div>`);
    setHtml($("docs"), html`${parts}`);
  }

  // Document / Changes / Actions --------------------------------------------------------------

  function renderDocument() {
    const doc = state.lastDocument;
    const body = $("document");
    if (!doc) {
      setHtml(body, html`<p class="empty">${t("doc.waiting")}</p>`);
      return;
    }
    setHtml(
      body,
      html`<p>${tHtml("doc.summary", {
        bytes: doc.bytes.toLocaleString(),
        ops: doc.operations,
        centered: $("layout").value === "centered" ? t("doc.centered") : "",
      })}</p>${codeBlock(t("doc.structure"), doc.structure, "plain")}`,
    );
  }

  function renderChanges() {
    const doc = state.lastDocument;
    const body = $("changes");
    if (!doc || !doc.diff) {
      setHtml(body, html`<p class="empty">${t("changes.none")}</p>`);
      return;
    }
    setHtml(body, html`<p>${tHtml("changes.intro")}</p>${codeBlock("", doc.diff, "diff")}`);
  }

  // Host values ------------------------------------------------------------------------------

  const NAMED_TYPES = { 0: "text", 1: "float", 2: "color", 3: "image", 4: "int", 5: "long", 6: "floats" };

  function hostInput(v, label) {
    const kind = NAMED_TYPES[v.type] || "unknown";
    const attrs = html`data-host="${v.name}" data-type="${v.type}" aria-label="${label}"`;
    if (kind === "text") return html`<input type="text" ${attrs} placeholder="${t("host.placeholder")}">`;
    if (kind === "float" || kind === "int" || kind === "long") {
      const step = kind === "float" ? "0.1" : "1";
      return html`<input type="number" ${attrs} step="${step}" placeholder="${t("host.placeholder")}">`;
    }
    if (kind === "color") return html`<input type="color" ${attrs} value="#3949ab">`;
    return html`<span class="empty">${t("host.inCode", { kind })}</span>`;
  }

  function renderHostValues() {
    const body = $("host");
    const doc = state.lastDocument;
    const named = (doc && doc.named) || [];
    if (!named.length) {
      setHtml(body, html`<p class="empty">${tHtml("host.none")}</p>`);
      return;
    }
    const rows = named.map((v) => {
      const label = v.name.replace(/^USER:/, "");
      const kind = NAMED_TYPES[v.type] || "unknown";
      return html`<tr><td class="name">${label}</td><td><code>${kind}</code></td><td><div class="control">${hostInput(v, label)}</div></td></tr>`;
    });
    setHtml(
      body,
      html`<p>${tHtml("host.intro", { bytes: doc.bytes.toLocaleString() })}</p>
      <table class="controls"><thead><tr><th>${t("controls.name")}</th><th>${t("host.type")}</th><th>${t("host.value")}</th></tr></thead><tbody>${rows}</tbody></table>
      <p><button class="btn" id="hostReset">${t("host.reset")}</button></p>`,
    );
    body.querySelectorAll("[data-host]").forEach((el) => {
      el.addEventListener("input", () => {
        const type = Number(el.dataset.type);
        const value = type === 2 ? "#FF" + el.value.slice(1).toUpperCase() : el.value;
        window.rcStorybook.hostValue(el.dataset.host, type, value);
        $("hostState").textContent = t("host.state");
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
      setHtml(body, html`<p class="empty">${t("actions.none")}</p>`);
      return;
    }
    setHtml(
      body,
      html`<ul class="action-log">${state.actions
        .slice()
        .reverse()
        .map((a) => html`<li><time>${a.time}</time><b>${a.type}</b><span>${a.detail}</span></li>`)}</ul>
      <p><button class="btn" id="clearActions">${t("actions.clear")}</button></p>`,
    );
    $("clearActions").addEventListener("click", () => {
      state.actions = [];
      updateActionsBadge();
      renderActions();
    });
  }

  // Pages ------------------------------------------------------------------------------------

  // The AndroidX release the API reference is pinned to ({artifact: version}), or null when it
  // follows androidx-main.
  function pinnedRelease() {
    const api = state.meta.androidxApi;
    return api && api["remote-creation-compose"] ? api : null;
  }

  // Where the API reference comes from, as HTML for the Coverage page.
  function apiSource() {
    const commit = (state.coverage.commit || "").slice(0, 9);
    const pinned = pinnedRelease();
    return pinned
      ? html`<code>${pinned["remote-creation-compose"]}</code>, androidx <code>${commit}</code>`
      : html`androidx-main <code>${commit}</code>`;
  }

  function renderOverview() {
    const pinned = pinnedRelease();
    const total = state.components.reduce((n, c) => n + c.stories.length, 0);
    // Coverage is decided at build time (tools/storybook_tools.py coverage_report); this only shows it.
    const summary = state.coverage ? state.coverage.summary : { covered: 0, total: 0 };
    const cats = [...new Set(state.components.map((c) => c.category))].sort(
      (a, b) => CATEGORY_ORDER.indexOf(a) - CATEGORY_ORDER.indexOf(b),
    );
    const first = (c) => (c.stories[0] ? "#/story/" + c.stories[0].id : "#/");
    return html`
      <h1>Remote Compose Storybook</h1>
      <p class="lede">${tHtml("ov.lede")}</p>
      <p>${tHtml("ov.counts", { components: state.components.length, stories: total, covered: summary.covered, blocks: summary.total })}</p>

      <h2>${t("ov.what.title")}</h2>
      <p>${t("ov.what")}</p>
      <div class="audience">
        <div><h3>${t("ov.designers.title")}</h3><p>${t("ov.designers")}</p></div>
        <div><h3>${t("ov.pm.title")}</h3><p>${tHtml("ov.pm")}</p></div>
        <div><h3>${t("ov.dev.title")}</h3><p>${t("ov.dev")}</p></div>
      </div>

      <h2>${t("ov.read.title")}</h2>
      <p>${tHtml("ov.read")}</p>

      <h2>${t("ov.use.title")}</h2>
      ${pinned ? html`<p>${tHtml("ov.useRelease", { release: pinned["remote-creation-compose"] })}</p>` : html`<p>${tHtml("ov.use", { release: state.meta.androidxRelease || "Google Maven" })}</p>`}
      ${codeBlock(
        "build.gradle.kts",
        pinned
          ? `repositories {
    google()
}

dependencies {
    // Author documents (the API in this catalog)
    implementation("androidx.compose.remote:remote-creation-compose:${pinned["remote-creation-compose"]}")
    implementation("androidx.compose.remote.foundation:foundation:${pinned.foundation}")
    // Play documents on Android
    implementation("androidx.compose.remote:remote-player-compose:${pinned["remote-creation-compose"]}")
}`
          : `repositories {
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
        t("ov.capture"),
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

      <h2>${t("ov.components")}</h2>
      ${cats.map(
        (cat) =>
          html`<h3>${I18n.category(cat)}</h3><div class="cards">${state.components
            .filter((c) => c.category === cat)
            .map((c) => {
              const plain = I18n.plain(c);
              return html`<a class="card" href="${first(c)}"><b>${c.title}</b><span>${plain ? plain.what : c.description}</span></a>`;
            })}</div>`,
      )}

      <h2>${t("ov.how.title")}</h2>
      <p>${tHtml("ov.how")}</p>`;
  }

  function renderCoverage() {
    if (!state.coverage) return html`<h1>${t("cov.title")}</h1><p class="empty">${t("cov.missing")}</p>`;
    // Coverage is decided at build time (tools/storybook_tools.py coverage_report): each block's
    // status, its stories and the control option codes that use it. This page only shows it.
    const { blocks, summary } = state.coverage;
    const storyFor = new Map();
    for (const s of state.storiesById.values()) if (!storyFor.has(s.function)) storyFor.set(s.function, s);
    // Stories offering one of these option codes in a control.
    const viaOptions = (codes) => {
      const out = [];
      for (const s of state.storiesById.values()) {
        if (s.args.some((a) => a.options && a.options.some((o) => codes.includes(o.code)))) out.push(s);
      }
      return out;
    };
    const { covered, total, pending } = summary;
    const groups = new Map();
    for (const b of blocks) {
      if (!groups.has(b.group)) groups.set(b.group, []);
      groups.get(b.group).push(b);
    }
    const parts = [
      html`<h1>${t("cov.title")}</h1>
      <p class="lede">${tHtml("cov.lede", { source: apiSource() })}</p>
      <p>${tHtml("cov.summary", { covered, total, pending: pending ? tHtml("cov.pendingSummary", { n: pending }) : "" })}</p>
      <div class="meter"><div style="width:${((100 * covered) / Math.max(1, total)).toFixed(1)}%"></div></div>`,
    ];
    for (const [group, items] of groups) {
      const n = items.filter((b) => b.status === "covered").length;
      parts.push(
        html`<div class="cov-group"><h2>${group} <span class="pill">${n}/${items.length}</span></h2><table class="cov-table">`,
      );
      for (const b of items) {
        const direct = b.stories.map((fn) => storyFor.get(fn)).filter(Boolean);
        const opts = viaOptions(b.optionCodes).filter((s) => !direct.includes(s));
        const links = [...direct, ...opts].map((s) => html`<a href="#/story/${s.id}">${s.name}</a>`);
        // A covered block with no story page to link: its stories are helper functions only.
        const note =
          b.status === "pending"
            ? html`<span class="pill">${t("cov.pending")}</span> <span class="empty">${b.reason}</span>`
            : b.status === "covered"
              ? html`<span class="empty">${t("cov.unlinked")}</span> ${b.stories.map((fn) => html`<code>${fn}</code> `)}`
              : html`<span class="empty">${t("cov.none")}</span>`;
        parts.push(html`<tr><td>${b.name}</td><td>${links.length ? links : note}</td></tr>`);
      }
      parts.push(html`</table></div>`);
    }
    return html`${parts}`;
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
      toast(t("copied"));
    } catch {
      toast(t("copyFailed"));
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
      setTimeout(() => copyText(shareUrl()), 300);
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
      $("docStats").textContent = t("stats", { bytes: doc.bytes.toLocaleString(), ops: doc.operations });
      const { component, story } = state.current;
      $("stage").setAttribute("aria-label", `Stage: ${component.title}, ${story.name}`);
      $("stageText").textContent = (doc.texts || []).length
        ? t("stage.text", { texts: doc.texts.join("; ") })
        : (I18n.plain(component) && I18n.plain(component).what) || story.description;
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
      overlay(t("stage.failed", { message: f.message }), true);
      document.body.dataset.failed = f.story;
    });
  }

  window.addEventListener("rcstorybook:fonts", () => {
    document.body.dataset.fonts = "ready";
  });

  // A crash inside the Wasm player leaves its canvas dead: say so rather than showing a blank stage.
  window.addEventListener("error", (e) => {
    // Only errors from the Wasm bundle; the shell's own errors are not the player's.
    if (!state.current || !/storybook\.js|\.wasm/.test(e.filename || "")) return;
    overlay(t("stage.crashed", { message: e.message || "unknown error" }), true);
  });

  boot();
})();
