// End-to-end check of the built site (build/site), in a real browser:
//  1. every story composes, captures and plays without a failure;
//  2. every component's Docs tab finds its AndroidX signatures;
//  3. every story's Code tab has its source;
//  4. changing a control produces a new document and a structural diff;
//  5. tapping a host-action story reaches the Actions tab;
//  6. pages (overview, coverage) render.
// Screenshots of every story land in e2e/out/ for review.
//
//   node e2e/run.mjs [--site build/site] [--only <story-id-substring>]
import { chromium } from "playwright";
import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import pixelmatch from "pixelmatch";
import { PNG } from "pngjs";
import AxeBuilder from "@axe-core/playwright";

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

const types = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".mjs": "text/javascript",
  ".css": "text/css",
  ".json": "application/json",
  ".wasm": "application/wasm",
  ".png": "image/png",
  ".svg": "image/svg+xml",
};
const server = http.createServer((req, res) => {
  let url;
  try {
    url = decodeURIComponent(req.url.split("?")[0]);
  } catch {
    res.writeHead(400);
    res.end("bad request");
    return;
  }
  let file = path.join(site, url === "/" ? "index.html" : url);
  if (!file.startsWith(site) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
    res.writeHead(404);
    res.end("not found");
    return;
  }
  res.writeHead(200, { "content-type": types[path.extname(file)] || "application/octet-stream" });
  fs.createReadStream(file).pipe(res);
});
await new Promise((r) => server.listen(0, "127.0.0.1", r));
const base = `http://127.0.0.1:${server.address().port}/`;

const failures = [];
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

async function openStory(id, query = "") {
  await page.evaluate(() => {
    delete document.body.dataset.rendered;
    delete document.body.dataset.failed;
  });
  await page.goto(base + "#/story/" + id + query);
  const result = await page
    .waitForFunction(
      (sid) => (document.body.dataset.rendered === sid ? "ok" : document.body.dataset.failed === sid ? "failed" : null),
      id,
      { timeout: 20000 },
    )
    .then((h) => h.jsonValue())
    .catch(() => "timeout");
  return result;
}

for (const s of stories) {
  if (only && !s.id.includes(only)) continue;
  const errorsBefore = pageErrors.length;
  const result = await openStory(s.id);
  if (result !== "ok") {
    const overlay = await page.textContent("#stageOverlay").catch(() => "");
    fail(`${s.id}: ${result} ${overlay.trim().slice(0, 300)}`);
  }
  await page.waitForTimeout(400);
  await page.locator("#stageWrap").screenshot({ path: path.join(outDir, s.id + ".png") });
  if (!timeDriven(s.function)) {
    const stage = await page.locator("#stageFrame").screenshot();
    const baseline = path.join(baselinesDir, s.id + ".png");
    if (updateBaselines) {
      fs.mkdirSync(baselinesDir, { recursive: true });
      fs.writeFileSync(baseline, stage);
    } else if (compareBaselines && fs.existsSync(baseline)) {
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
  await page.waitForTimeout(500);
  const box = await page.locator("#stageFrame").boundingBox();
  await page.mouse.click(box.x + 40, box.y + 25);
  await page.waitForTimeout(800);
  const count = await page.textContent("#actionsCount");
  if (!(Number(count) > 0)) fail("tapping hostAction raised no event in the Actions tab");
  else console.log(`  ok host action (${count} event)`);
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
  const cardBefore = await page.locator("#stageFrame").screenshot();
  await page.click('[data-tab="host"]');
  await page.fill('[data-host="USER:title"]', "Live from the host");
  await page.waitForTimeout(800);
  const cardAfter = await page.locator("#stageFrame").screenshot();
  const statsAfter = await page.textContent("#docStats");
  if (Buffer.compare(cardBefore, cardAfter) === 0) fail("a host value changed nothing on the stage");
  else if (statsBefore !== statsAfter) fail(`a host value made a new document (${statsBefore} → ${statsAfter})`);
  else console.log("  ok host values replay the same document");
  await setLayout("topstart");

  // A tap on a valueChangeAction changes what is drawn.
  await openStory("clickable--counter");
  await page.waitForTimeout(400);
  const c = await page.locator("#stageFrame").boundingBox();
  const shotA = await page.locator("#stageFrame").screenshot();
  await page.mouse.click(c.x + 28, c.y + 28); // the "−" button at the row's start
  await page.waitForTimeout(600);
  const shotB = await page.locator("#stageFrame").screenshot();
  if (Buffer.compare(shotA, shotB) === 0) fail("tapping the counter's − changed nothing on the stage");
  else console.log("  ok valueChangeAction redraws");
}

await browser.close();
server.close();
console.log(failures.length ? `\n${failures.length} failure(s)` : "\nall checks passed");
process.exit(failures.length ? 1 : 0);
