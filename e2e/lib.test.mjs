// The harness itself (lib.mjs), against a stand-in page that speaks the storybook's test protocol
// (web/app.js: data-showing and data-rendered on <body>) and reproduces the race of ardmn/work#157:
// after a new hash the stage first reports and draws the previous args' document, and the one for
// the new args only follows later. Run with `npm run test:harness`.
import { test, before, after } from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { chromium } from "playwright";
import { PNG } from "pngjs";
import { serveSite, stageTools } from "./lib.mjs";

// The page: "#/story/<id>?c=<css colour>" shows story <id> painted in that colour. On every new hash
// it reports the previous args' document at once (and draws it), and the new one after LATE_MS.
// "#/story/clock" never stops changing; "#/story/broken" fails. window.control(c) changes the colour
// in place, as a control does (no hash change), with the same late document.
const LATE_MS = 600;
const PAGE = `<!doctype html>
<html><body>
<div id="stageFrame" style="width: 40px; height: 40px"></div>
<script>
  const frame = document.getElementById("stageFrame");
  let previous = null;
  let timer = 0;
  function show() {
    const [path, query] = location.hash.slice(1).split("?");
    const id = path.replace("/story/", "");
    clearTimeout(timer);
    if (id === "broken") {
      document.body.dataset.failed = id;
      return;
    }
    if (id === "clock") {
      let n = 0;
      setInterval(() => (frame.style.background = "rgb(" + (n++ % 256) + ", 0, 0)"), 50);
      return;
    }
    play(id, new URLSearchParams(query || "").get("c") || "white");
  }
  function play(id, color) {
    const args = JSON.stringify({ c: color });
    document.body.dataset.showing = id + " " + args;
    if (previous && previous.id === id) {
      frame.style.background = previous.color;
      document.body.dataset.rendered = id + " " + previous.args;
    }
    timer = setTimeout(() => {
      frame.style.background = color;
      document.body.dataset.rendered = id + " " + args;
      previous = { id, color, args };
    }, ${LATE_MS});
  }
  window.control = (color) => play("ring", color);
  addEventListener("hashchange", show);
  show();
</script>
</body></html>`;

let browser, server, page, tools, dir;

before(async () => {
  dir = fs.mkdtempSync(path.join(os.tmpdir(), "rcsb-harness-"));
  fs.writeFileSync(path.join(dir, "index.html"), PAGE);
  server = await serveSite(dir);
  browser = await chromium.launch();
  page = await browser.newPage();
  await page.goto(server.base);
  tools = stageTools(page, server.base);
});

after(async () => {
  await browser?.close();
  server?.close();
  if (dir) fs.rmSync(dir, { recursive: true, force: true });
});

const centre = (png) => {
  const img = PNG.sync.read(png);
  const i = ((img.height >> 1) * img.width + (img.width >> 1)) * 4;
  return [...img.data.slice(i, i + 3)];
};

test("openStory waits for the document of the new args, not the previous args' one", async () => {
  assert.equal(await tools.openStory("ring", "?c=rgb(255,0,0)"), "ok");
  assert.deepEqual(centre((await tools.stableStage()).png), [255, 0, 0]);
  // Same story, new args: the stage reports and draws red again before blue arrives.
  assert.equal(await tools.openStory("ring", "?c=rgb(0,0,255)"), "ok");
  const { png, settled } = await tools.stableStage();
  assert.equal(settled, true);
  assert.deepEqual(centre(png), [0, 0, 255]);
});

test("openStory of the address the page is already on still renders it", async () => {
  assert.equal(await tools.openStory("ring", "?c=rgb(0,128,0)"), "ok");
  assert.equal(await tools.openStory("ring", "?c=rgb(0,128,0)"), "ok");
  assert.deepEqual(centre((await tools.stableStage()).png), [0, 128, 0]);
});

test("waitRendered after a control change waits for the new args", async () => {
  assert.equal(await tools.openStory("ring", "?c=rgb(255,0,0)"), "ok");
  await tools.forgetRendered();
  await page.evaluate(() => window.control("rgb(0,0,255)"));
  assert.equal(await tools.waitRendered("ring", 5000), "ok");
  assert.deepEqual(centre((await tools.stableStage()).png), [0, 0, 255]);
});

test("openStory reports a story that failed", async () => {
  assert.equal(await tools.openStory("broken"), "failed");
});

test("stableStage says when the stage never settled", async () => {
  await page.goto(server.base + "#/story/clock");
  const { png, settled } = await tools.stableStage(1000);
  assert.equal(settled, false);
  assert.ok(Buffer.isBuffer(png));
});
