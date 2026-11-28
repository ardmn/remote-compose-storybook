// Shared by the end-to-end test (run.mjs) and the link-preview generator (social.mjs): a local
// server for the built site and the helpers that open a story and capture its stage.
import http from "node:http";
import fs from "node:fs";
import path from "node:path";

const types = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".mjs": "text/javascript",
  ".css": "text/css",
  ".json": "application/json",
  ".wasm": "application/wasm",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".svg": "image/svg+xml",
  ".ttf": "font/ttf",
};

// Serves `site` the way GitHub Pages does, including a directory's index.html.
export async function serveSite(site) {
  const server = http.createServer((req, res) => {
    let url;
    try {
      url = decodeURIComponent(req.url.split("?")[0]);
    } catch {
      res.writeHead(400);
      res.end("bad request");
      return;
    }
    let file = path.join(site, url);
    if (file.startsWith(site) && fs.existsSync(file) && fs.statSync(file).isDirectory()) {
      file = path.join(file, "index.html");
    }
    if (!file.startsWith(site) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
      res.writeHead(404);
      res.end("not found");
      return;
    }
    res.writeHead(200, { "content-type": types[path.extname(file)] || "application/octet-stream" });
    fs.createReadStream(file).pipe(res);
  });
  await new Promise((r) => server.listen(0, "127.0.0.1", r));
  return { base: `http://127.0.0.1:${server.address().port}/`, close: () => server.close() };
}

export function stageTools(page, base) {
  // The stage as pixels: its own size, at whole-pixel coordinates. An element screenshot rounds a
  // fractional position into a 1px taller image (a block above the stage moved it by half a pixel).
  async function stageShot() {
    const clip = await page.evaluate(() => {
      const frame = document.getElementById("stageFrame");
      const r = frame.getBoundingClientRect();
      return {
        x: Math.round(r.left + window.scrollX),
        y: Math.round(r.top + window.scrollY),
        width: parseInt(frame.style.width, 10),
        height: parseInt(frame.style.height, 10),
      };
    });
    return page.screenshot({ clip, fullPage: true });
  }

  // The stage once it has stopped changing: two screenshots in a row that match, as
  // { png, settled: true }. A document arriving is not the same as its first frame being drawn, so
  // a fixed pause would race the player. A stage that keeps moving (a clock) is returned as it was
  // when time ran out, with settled: false, so a caller that compares pixels can refuse it.
  async function stableStage(timeoutMs = 10000) {
    let previous = await stageShot();
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      await page.waitForTimeout(150);
      const next = await stageShot();
      if (Buffer.compare(previous, next) === 0) return { png: next, settled: true };
      previous = next;
    }
    return { png: previous, settled: false };
  }

  // Polls until the stage differs from `before`; returns whether it did within the timeout.
  async function stageChangesFrom(before, timeoutMs = 5000) {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      if (Buffer.compare(before, await stageShot()) !== 0) return true;
      await page.waitForTimeout(100);
    }
    return false;
  }

  // Stories that load from the network get the copy this server serves, so nothing needs the net.
  function localArgs(s) {
    const params = new URLSearchParams();
    for (const a of s.args) {
      if (a.type === "text" && /^https:\/\/raw\.githubusercontent\.com\/.*\/web\/(.*)$/.test(a.default)) {
        params.set(a.name, base + a.default.replace(/^.*\/web\//, ""));
      }
    }
    const q = params.toString();
    return q ? "?" + q : "";
  }

  // Forgets what the page last showed and rendered, before an action that shows something new: a
  // document from before it can then never pass for the new one.
  async function forgetRendered() {
    await page.evaluate(() => {
      delete document.body.dataset.showing;
      delete document.body.dataset.rendered;
      delete document.body.dataset.failed;
    });
  }

  // Waits until the page shows story `id` and the stage has its document for exactly the args the
  // page asked for (web/app.js: data-showing and data-rendered are "<story> <args>"), or the story
  // failed: "ok", "failed" or "timeout". Call forgetRendered() before the action that shows it.
  // Assumes no document for older args arrives after the current one: args going A -> B -> A
  // within one wait could match the first A's late document (no e2e flow does that).
  async function waitRendered(id, timeoutMs = 20000) {
    return page
      .waitForFunction(
        (sid) => {
          const { showing, rendered, failed } = document.body.dataset;
          if (failed === sid) return "failed";
          return showing?.startsWith(sid + " ") && rendered === showing ? "ok" : null;
        },
        id,
        { timeout: timeoutMs },
      )
      .then((h) => h.jsonValue())
      .catch(() => "timeout");
  }

  // Opens a story and waits until it has rendered its args or failed: "ok", "failed" or "timeout".
  async function openStory(id, query = "") {
    await forgetRendered();
    await page.goto(base + "#/story/" + id + query);
    return waitRendered(id);
  }

  return { stageShot, stableStage, stageChangesFrom, localArgs, openStory, forgetRendered, waitRendered };
}
