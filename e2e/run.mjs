// End-to-end check of the built site (build/site), in a real browser:
//  1. every story composes, captures and plays without a failure;
//  2. every component's Docs tab finds its AndroidX signatures;
//  3. every story's Code tab has its source;
//  4. changing a control produces a new document and a structural diff;
//  5. tapping a host-action story reaches the Actions tab;
//  6. pages (overview, coverage) render;
//  7. each story's static page for search engines (s/<id>/, s/<id>/ru/), sitemap.xml and llms.txt.
// Screenshots of every story land in e2e/out/ for review.
//
//   node e2e/run.mjs [--site build/site] [--only <story-id-substring>]
import { chromium } from "playwright";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import pixelmatch from "pixelmatch";
import { PNG } from "pngjs";
import AxeBuilder from "@axe-core/playwright";
import { serveSite, stageTools } from "./lib.mjs";

const here = path.dirname(fileURLToPath(import.meta.url));
const args = process.argv.slice(2);
const opt = (name, def) => {
  const i = args.indexOf(name);
  return i >= 0 ? args[i + 1] : def;
};
const site = path.resolve(here, "..", opt("--site", "build/site"));
const only = opt("--only", "");
const outDir = path.join(here, "out");
// Visual regression: each story's stage against e2e/baselines/<id>.png. Baselines come from CI
// (Linux), where rendering is the same run to run; elsewhere they are compared only on request.
const baselinesDir = path.join(here, "baselines");
const compareBaselines = args.includes("--compare-baselines") || !!process.env.CI;
const updateBaselines = args.includes("--update-baselines");
// Stories whose drawing follows the clock cannot have a baseline.
const sources = JSON.parse(fs.readFileSync(path.join(site, "data", "sources.json"), "utf8"));
const timeDriven = (fn) =>
  /RemoteTimeVariables|continuousSeconds|animateRemote|remoteSpring/.test(sources[fn]?.code ?? "");
fs.mkdirSync(outDir, { recursive: true });

const { base, close: closeServer } = await serveSite(site);

const failures = [];
let compared = 0;
const fail = (what) => {
  failures.push(what);
  console.log("  FAIL", what);
};

const browser = await chromium.launch();
// A context, not browser.newPage(): the accessibility audit opens pages in it.
const context = await browser.newContext({ viewport: { width: 1360, height: 1000 } });
const page = await context.newPage();
const pageErrors = [];
page.on("pageerror", (e) => pageErrors.push(e.message));
page.on("console", (m) => {
  if (m.type() === "error") pageErrors.push(m.text());
});

await page.goto(base);
await page.waitForFunction(() => !!window.rcStorybook, null, { timeout: 60000 });
// Text drawn before the fonts arrive uses a fallback face: nothing is compared until they have.
await page.waitForFunction(() => document.body.dataset.fonts === "ready", null, { timeout: 60000 });
const catalog = JSON.parse(await page.evaluate(() => window.rcStorybook.catalog()));
const stories = catalog.components.flatMap((c) => c.stories.map((s) => ({ ...s, component: c })));
console.log(`${catalog.components.length} components, ${stories.length} stories at ${base}`);

// Pages.
await page.waitForSelector("#page h1");
if (!(await page.textContent("#page h1")).includes("Remote Compose Storybook")) fail("overview page did not render");
await page.screenshot({ path: path.join(outDir, "_overview.png") });
await page.goto(base + "#/coverage");
await page.waitForSelector(".cov-table");
await page.screenshot({ path: path.join(outDir, "_coverage.png"), fullPage: true });

// The stage layout for the next story; the control may be hidden (on a page without a stage).
async function setLayout(value) {
  await page.evaluate((v) => {
    const el = document.getElementById("layout");
    el.value = v;
    el.dispatchEvent(new Event("change"));
  }, value);
}

const { stableStage, stageChangesFrom, localArgs, openStory } = stageTools(page, base);

for (const s of stories) {
  if (only && !s.id.includes(only)) continue;
  const errorsBefore = pageErrors.length;
  const result = await openStory(s.id, localArgs(s));
  if (result !== "ok") {
    const overlay = await page.textContent("#stageOverlay").catch(() => "");
    fail(`${s.id}: ${result} ${overlay.trim().slice(0, 300)}`);
  }
  const settled = timeDriven(s.function) ? null : await stableStage();
  await page.locator("#stageWrap").screenshot({ path: path.join(outDir, s.id + ".png") });
  if (settled) {
    const stage = settled;
    const baseline = path.join(baselinesDir, s.id + ".png");
    if (updateBaselines) {
      fs.mkdirSync(baselinesDir, { recursive: true });
      fs.writeFileSync(baseline, stage);
    } else if (compareBaselines && fs.existsSync(baseline)) {
      compared++;
      const want = PNG.sync.read(fs.readFileSync(baseline));
      const got = PNG.sync.read(stage);
      if (want.width !== got.width || want.height !== got.height) {
        fail(`${s.id}: stage is ${got.width}×${got.height}, baseline ${want.width}×${want.height}`);
      } else {
        const diff = new PNG({ width: got.width, height: got.height });
        const changed = pixelmatch(want.data, got.data, diff.data, got.width, got.height, { threshold: 0.15 });
        const ratio = changed / (got.width * got.height);
        if (ratio > 0.005) {
          fs.writeFileSync(path.join(outDir, s.id + ".diff.png"), PNG.sync.write(diff));
          fs.writeFileSync(path.join(outDir, s.id + ".actual.png"), stage);
          fail(`${s.id}: ${(ratio * 100).toFixed(2)}% of the stage differs from its baseline (see ${s.id}.diff.png)`);
        }
      }
    }
  }
  // Every tab can be opened, and the tab bar stays visible whichever is open.
  if (s === stories.find((x) => !only || x.id.includes(only))) {
    for (const tab of ["code", "docs", "document", "changes", "host", "actions", "controls"]) {
      await page.click(`[data-tab="${tab}"]`);
      // The tab bar itself: its buttons keep their height even when the bar is squeezed to nothing.
      const bar = await page.evaluate(() => document.querySelector(".tabs").getBoundingClientRect().height);
      const shown = await page.evaluate((t) => !document.querySelector(`[data-body="${t}"]`).hidden, tab);
      if (bar < 30 || !shown) fail(`${s.id}: after opening "${tab}" the tab bar is ${bar.toFixed(0)}px high`);
    }
  }
  // Code tab: the story's source must be there.
  const code = await page.textContent("#code");
  if (!code.includes("fun " + s.function + "(")) fail(`${s.id}: Code tab lacks the source of ${s.function}`);
  // Docs tab: every symbol must resolve.
  const docs = await page.textContent("#docs");
  if (docs.includes("No signature found")) fail(`${s.id}: Docs tab has a symbol with no AndroidX signature`);
  if (pageErrors.length > errorsBefore)
    fail(`${s.id}: page errors: ${pageErrors.slice(errorsBefore).join(" | ").slice(0, 300)}`);
  console.log(`  ok ${s.id}`);
}

// A control change makes a new document and a diff.
if (!only) {
  await openStory("remote-row--weights");
  const before = await page.textContent("#docStats");
  await page.evaluate(() => {
    delete document.body.dataset.rendered;
  });
  await page.locator('[data-arg="firstWeight"]').fill("4");
  await page.waitForFunction(() => document.body.dataset.rendered === "remote-row--weights", null, { timeout: 10000 });
  await page.click('[data-tab="changes"]');
  const diff = await page.textContent("#changes");
  // A hunk header and at least one changed line of the document.
  if (!/@@ -\d+,\d+ \+\d+,\d+ @@/.test(diff) || !/\n[+-](?![+-]{2})/.test(diff))
    fail("changing firstWeight produced no structural diff");
  const recorded = await page
    .waitForFunction(() => location.hash.includes("firstWeight=4"), null, { timeout: 3000 })
    .then(() => true)
    .catch(() => false);
  if (!recorded) fail("the URL did not record the changed arg: " + page.url());
  await page.screenshot({ path: path.join(outDir, "_changes.png") });
  console.log(`  ok control change (${before} → ${await page.textContent("#docStats")})`);

  // Taps below aim at fixed points of the story, so lay it out from the top start.
  await setLayout("topstart");
  // A tap on a host action reaches the Actions tab.
  await openStory("host-action--named");
  await stableStage();
  const box = await page.locator("#stageFrame").boundingBox();
  await page.mouse.click(box.x + 40, box.y + 25);
  const raised = await page
    .waitForFunction(() => Number(document.getElementById("actionsCount").textContent) > 0, null, { timeout: 5000 })
    .then(() => true)
    .catch(() => false);
  if (!raised) fail("tapping hostAction raised no event in the Actions tab");
  else console.log(`  ok host action (${await page.textContent("#actionsCount")} event)`);
  await page.click('[data-tab="actions"]');
  await page.screenshot({ path: path.join(outDir, "_actions.png") });

  // A story at rest sends no further documents (a recomposition loop would, many per second).
  await openStory("remote-row--arrangement");
  const storm = await page.evaluate(
    () =>
      new Promise((resolve) => {
        let count = 0;
        const on = () => count++;
        window.addEventListener("rcstorybook:document", on);
        setTimeout(() => {
          window.removeEventListener("rcstorybook:document", on);
          resolve(count);
        }, 2000);
      }),
  );
  if (storm > 0) fail(`a story at rest sent ${storm} documents in 2s`);
  else console.log("  ok a story at rest is quiet");

  // Accessibility: no serious or critical axe violation on the overview, a story or the coverage page.
  for (const hash of ["#/", "#/story/recipe-product-card--default", "#/coverage"]) {
    if (hash.startsWith("#/story/")) await openStory(hash.slice("#/story/".length));
    else {
      await page.goto(base + hash);
      await page.waitForSelector("#page h1");
    }
    const audit = await new AxeBuilder({ page }).analyze();
    const bad = audit.violations.filter((v) => v.impact === "serious" || v.impact === "critical");
    if (bad.length) fail(`${hash}: ${bad.map((v) => `${v.id} (${v.nodes.length})`).join(", ")}`);
    else console.log(`  ok accessibility ${hash}`);
  }

  // Returning to the same story from a page without a stage shows its document again.
  await setLayout("centered");
  await openStory("remote-box--layering");
  await page.goto(base + "#/");
  await page.waitForSelector("#page h1");
  if ((await openStory("remote-box--layering")) !== "ok") fail("returning to the same story left it rendering");
  else console.log("  ok return to the same story");

  // Args from a URL are clamped or defaulted, and the Code tab prints what the player draws.
  await openStory("remote-box--content-alignment", "?boxSize=99999&childSize=abc&contentAlignment=Bogus");
  const usage = await page.textContent("#code pre");
  if (
    !usage.includes("boxSize = 320") ||
    !usage.includes("childSize = 72") ||
    !usage.includes("RemoteAlignment.Center")
  )
    fail("URL args were not normalised in the Code tab: " + usage.slice(0, 200));
  else console.log("  ok URL args normalised");

  // Nothing from the URL is ever markup: no element or event-handler attribute appears in the
  // controls, whatever the arg's type.
  const injected = () =>
    page.evaluate(() =>
      [...document.querySelectorAll("#controls *")].some(
        (el) => el.tagName === "IMG" || [...el.attributes].some((at) => at.name.startsWith("on")),
      ),
    );
  await openStory("remote-box--empty", "?color=" + encodeURIComponent('FF"><img src=x onerror="alert(1)">'));
  const viaColor = await injected();
  await openStory("remote-text--basics", "?text=" + encodeURIComponent('x" onfocus="alert(1)" y="'));
  const viaText = await injected();
  if (viaColor || viaText) fail(`a URL arg injected markup (color: ${viaColor}, text: ${viaText})`);
  else console.log("  ok URL args are escaped");
  await setLayout("topstart");

  // Host values replay the same document with values set by name.
  await setLayout("centered");
  await openStory("named-state--card");
  const statsBefore = await page.textContent("#docStats");
  const cardBefore = await stableStage();
  await page.click('[data-tab="host"]');
  await page.fill('[data-host="USER:title"]', "Live from the host");
  const cardChanged = await stageChangesFrom(cardBefore);
  const statsAfter = await page.textContent("#docStats");
  if (!cardChanged) fail("a host value changed nothing on the stage");
  else if (statsBefore !== statsAfter) fail(`a host value made a new document (${statsBefore} → ${statsAfter})`);
  else console.log("  ok host values replay the same document");
  await setLayout("topstart");

  // A tap on a valueChangeAction changes what is drawn.
  await openStory("clickable--counter");
  const shotA = await stableStage();
  const c = await page.locator("#stageFrame").boundingBox();
  await page.mouse.click(c.x + 28, c.y + 28); // the "−" button at the row's start
  if (!(await stageChangesFrom(shotA))) fail("tapping the counter's − changed nothing on the stage");
  else console.log("  ok valueChangeAction redraws");
}

// Link previews, written by social.mjs: a page per story whose Open Graph tags name its own card,
// and which sends a browser on to the story with the link's args.
function jpegSize(buf) {
  for (let i = 2; i + 9 < buf.length;) {
    if (buf[i] !== 0xff) return null;
    const marker = buf[i + 1];
    if (marker >= 0xc0 && marker <= 0xc2) return { height: buf.readUInt16BE(i + 5), width: buf.readUInt16BE(i + 7) };
    i += 2 + buf.readUInt16BE(i + 2);
  }
  return null;
}
const shareDir = path.join(site, "s");
if (only) {
  // A partial run checks stories only.
} else if (!fs.existsSync(shareDir)) {
  if (process.env.CI) fail("no link previews: run social.mjs before the test");
  else console.log("  link previews not generated (node social.mjs)");
} else {
  const meta = (html, prop) => {
    const m = new RegExp(`<meta (?:property|name)="${prop}" content="([^"]*)"`).exec(html);
    return m ? m[1].replace(/&amp;/g, "&") : null;
  };
  const cardOf = (html) => {
    const image = meta(html, "og:image");
    const m = image && /^https?:\/\/[^/]+\/(?:[^?]*?\/)?((?:s\/[\w-]+\/card|social)\.jpg)\?v=/.exec(image);
    return m ? path.join(site, m[1]) : null;
  };
  const bad = [];
  for (const s of stories) {
    const file = path.join(shareDir, s.id, "index.html");
    if (!fs.existsSync(file)) {
      bad.push(`${s.id}: no page`);
      continue;
    }
    const html = fs.readFileSync(file, "utf8");
    const card = cardOf(html);
    const size = card && fs.existsSync(card) ? jpegSize(fs.readFileSync(card)) : null;
    if (!meta(html, "og:title")?.includes(s.component.title) || !meta(html, "og:description")) {
      bad.push(`${s.id}: title or description missing`);
    } else if (meta(html, "twitter:card") !== "summary_large_image") bad.push(`${s.id}: no large-image card`);
    else if (!size || size.width !== 1200 || size.height !== 630) bad.push(`${s.id}: card ${JSON.stringify(size)}`);
    else if (/http-equiv="refresh"/.test(html)) bad.push(`${s.id}: a meta refresh would take crawlers to index.html`);
  }
  const index = fs.readFileSync(path.join(site, "index.html"), "utf8");
  const siteCard = cardOf(index);
  if (!siteCard || !fs.existsSync(siteCard)) bad.push("index.html: no site card");
  if (bad.length) fail(`link previews: ${bad.slice(0, 5).join("; ")}${bad.length > 5 ? ` (+${bad.length - 5})` : ""}`);
  else console.log(`  ok ${stories.length} share pages with their own 1200x630 preview card`);

  // The share link opens the story with its args, and "Copy link" hands it out.
  const s = stories.find((x) => x.args.some((a) => a.type === "boolean"));
  const a = s.args.find((x) => x.type === "boolean");
  const value = a.default === "true" ? "false" : "true";
  await page.goto(`${base}s/${s.id}/?${a.name}=${value}`);
  const landed = await page
    .waitForFunction((sid) => document.body.dataset.rendered === sid, s.id, { timeout: 20000 })
    .then(() => true)
    .catch(() => false);
  const hash = await page.evaluate(() => location.hash);
  if (!landed || hash !== `#/story/${s.id}?${a.name}=${value}`) fail(`share page led to ${hash}`);
  else console.log("  ok a share link opens its story with the link's args");
  await page.evaluate(() => {
    navigator.clipboard.writeText = async (t) => {
      window.copied = t;
    };
  });
  await page.click("#copyLink");
  const copied = await page
    .waitForFunction(() => window.copied, null, { timeout: 5000 })
    .then((h) => h.jsonValue())
    .catch(() => null);
  if (copied !== `${base}s/${s.id}/?${a.name}=${value}`) fail(`"Copy link" copied ${copied}`);
  else console.log('  ok "Copy link" hands out the share page');

  // Static pages for search engines: real content, structured data, both languages, no redirect.
  const siteUrl = new URL(meta(index, "og:url") || "https://example.invalid/");
  const absolute = (p) => new URL(p, siteUrl).href;
  const seo = [];
  for (const st of stories) {
    for (const lang of ["en", "ru"]) {
      const rel = `s/${st.id}/${lang === "ru" ? "ru/" : ""}`;
      const file = path.join(site, rel, "index.html");
      if (!fs.existsSync(file)) {
        seo.push(`${rel}: no page`);
        continue;
      }
      const html = fs.readFileSync(file, "utf8");
      const canonical = /<link rel="canonical" href="([^"]+)"/.exec(html)?.[1];
      const lds = [...html.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)];
      let ld = null;
      try {
        ld = JSON.parse(lds[0]?.[1] ?? "null");
      } catch {
        // reported below
      }
      if (!html.startsWith("<!doctype html>") || !html.includes(`<html lang="${lang}">`)) seo.push(`${rel}: lang`);
      else if (canonical !== absolute(rel)) seo.push(`${rel}: canonical ${canonical}`);
      else if (!/hreflang="ru"/.test(html) || !/hreflang="x-default"/.test(html)) seo.push(`${rel}: hreflang`);
      else if (!/<h1>/.test(html) || !html.includes(st.component.title)) seo.push(`${rel}: no h1 with the title`);
      else if (!/<pre><code>[\s\S]*@RemoteComposable/.test(html)) seo.push(`${rel}: no code`);
      else if (!Array.isArray(ld) || ld[0]["@type"] !== "TechArticle" || ld[1]["@type"] !== "BreadcrumbList")
        seo.push(`${rel}: structured data`);
      else if (!fs.existsSync(path.join(site, "s", st.id, "stage.png"))) seo.push(`${rel}: no stage image`);
    }
  }
  const sitemapXml = fs.readFileSync(path.join(site, "sitemap.xml"), "utf8");
  const locs = [...sitemapXml.matchAll(/<loc>([^<]+)<\/loc>/g)].map((m) => m[1]);
  if (locs.length !== 1 + stories.length * 2) seo.push(`sitemap.xml: ${locs.length} URLs`);
  if (locs.some((l) => !l.startsWith(siteUrl.href))) seo.push("sitemap.xml: a URL outside the site");
  const llms = fs.readFileSync(path.join(site, "llms.txt"), "utf8");
  if (!llms.startsWith("# ") || stories.some((st) => !llms.includes(absolute(`s/${st.id}/`)))) {
    seo.push("llms.txt: a story is missing");
  }
  if (!index.includes(`href="s/${stories[0].id}/"`) || !/application\/ld\+json/.test(index)) {
    seo.push("index.html: no static catalog or structured data");
  }
  if (seo.length) fail(`static pages: ${seo.slice(0, 5).join("; ")}${seo.length > 5 ? ` (+${seo.length - 5})` : ""}`);
  else console.log(`  ok ${stories.length * 2} static pages (English, Russian), sitemap.xml and llms.txt`);

  // Without a query the page stays (search engines that run JavaScript index it); "play" opens the
  // story, and the Russian page opens it in Russian.
  await page.goto(`${base}s/${s.id}/`);
  await page.waitForTimeout(500);
  if (!page.url().endsWith(`/s/${s.id}/`)) fail(`the static page went on to ${page.url()}`);
  else console.log("  ok the static page stays a page");
  const audit = await new AxeBuilder({ page }).analyze();
  const badA11y = audit.violations.filter((v) => v.impact === "serious" || v.impact === "critical");
  if (badA11y.length) fail(`s/${s.id}/: ${badA11y.map((v) => `${v.id} (${v.nodes.length})`).join(", ")}`);
  else console.log("  ok accessibility of a static page");
  await page.goto(`${base}s/${s.id}/ru/?play`);
  await page.waitForURL(/#\/story\//, { timeout: 10000 }).catch(() => {});
  const ru = await page.evaluate(() => [location.search, location.hash, document.documentElement.lang]);
  if (ru[0] !== "?lang=ru" || ru[1] !== `#/story/${s.id}` || ru[2] !== "ru") fail(`?play led to ${ru.join(" ")}`);
  else console.log("  ok ?play opens the story, in the page's language");
  await page.evaluate(() => localStorage.removeItem("rcsb-lang"));
}

await browser.close();
closeServer();
console.log(
  compareBaselines
    ? `\n${compared} stories compared with their visual baselines`
    : "\nvisual baselines not compared (pass --compare-baselines, or run in CI)",
);
console.log(failures.length ? `\n${failures.length} failure(s)` : "\nall checks passed");
process.exit(failures.length ? 1 : 0);
