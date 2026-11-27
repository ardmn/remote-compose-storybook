// Checks of the Wear Material 3 stories: what a tap or a control change must do to what the
// player draws, and geometry a story's description promises. Called from run.mjs with the page
// laid out "topstart".
import fs from "node:fs";
import path from "node:path";
import pixelmatch from "pixelmatch";
import { PNG } from "pngjs";

// The width, in stage pixels, of the run of pixels around (x, y) that match the colour at (x, y):
// a filled button's width along one row.
function runWidth(png, x, y) {
  const at = (px) => {
    const i = (y * png.width + px) * 4;
    return [png.data[i], png.data[i + 1], png.data[i + 2]];
  };
  const [r, g, b] = at(x);
  const same = (px) => {
    const [r2, g2, b2] = at(px);
    return Math.abs(r - r2) + Math.abs(g - g2) + Math.abs(b - b2) < 60;
  };
  let left = x;
  let right = x;
  while (left > 0 && same(left - 1)) left--;
  while (right < png.width - 1 && same(right + 1)) right++;
  return right - left + 1;
}

// The default theme's primary, the colour of a progress indicator, as the player draws it.
const INDICATOR_RGB = [233, 221, 255];
// Per channel. Tight enough to leave out the stage's white and grey checkerboard (each off by 16
// or more in one channel) and the ring's anti-aliased edges.
const INDICATOR_TOLERANCE = 12;

// Whether the pixel at (x, y) of a decoded PNG is the indicator colour.
function isIndicator(png, x, y) {
  const i = (y * png.width + x) * 4;
  return INDICATOR_RGB.every((c, k) => Math.abs(png.data[i + k] - c) < INDICATOR_TOLERANCE);
}

// The centre and centre-line radius of a ring drawn in INDICATOR_RGB, from the bounding box of its
// pixels; null when there are none. Exact only for a closed ring: an open one's box is cut short.
function ringGeometry(png) {
  let [left, top, right, bottom] = [png.width, png.height, -1, -1];
  for (let y = 0; y < png.height; y++) {
    for (let x = 0; x < png.width; x++) {
      if (!isIndicator(png, x, y)) continue;
      left = Math.min(left, x);
      right = Math.max(right, x);
      top = Math.min(top, y);
      bottom = Math.max(bottom, y);
    }
  }
  if (right < 0) return null;
  const cx = (left + right) / 2;
  const cy = (top + bottom) / 2;
  // Walk from the top edge inwards along the centre column to find the stroke's inner edge.
  let inner = Math.round(top);
  while (inner < cy && isIndicator(png, Math.round(cx), inner)) inner++;
  return { cx, cy, radius: cy - (top + inner) / 2 };
}

// The gaps of a ring with the given geometry: runs of [from, to] degrees, clockwise from 3 o'clock,
// where the ring's centre line has no indicator pixel.
function ringGaps(png, { cx, cy, radius }) {
  const steps = 720;
  const missing = Array.from({ length: steps }, (_, step) => {
    const angle = (step / steps) * 2 * Math.PI;
    return !isIndicator(png, Math.round(cx + radius * Math.cos(angle)), Math.round(cy + radius * Math.sin(angle)));
  });
  const runs = [];
  for (let step = 0; step < steps; step++) {
    if (!missing[step]) continue;
    const last = runs.at(-1);
    if (last && last[1] === step - 1) last[1] = step;
    else runs.push([step, step]);
  }
  // A run through 0° is one gap, not two.
  if (runs.length > 1 && runs[0][0] === 0 && runs.at(-1)[1] === steps - 1) runs[0][0] = runs.pop()[0] - steps;
  return runs.map(([from, to]) => [(from * 360) / steps, ((to + 1) * 360) / steps]);
}

// The stroke's radial width, in pixels, at `degrees` clockwise from 3 o'clock on a ring with the
// given geometry: indicator pixels along the radius, sampled every quarter pixel.
function strokeWidthAt(png, { cx, cy, radius }, degrees) {
  const angle = (degrees * Math.PI) / 180;
  let hits = 0;
  for (let d = radius - 10; d <= radius + 10; d += 0.25) {
    if (isIndicator(png, Math.round(cx + d * Math.cos(angle)), Math.round(cy + d * Math.sin(angle)))) hits++;
  }
  return hits / 4;
}

// The number of pixels that differ between two same-size PNG screenshots.
function differingPixels(a, b) {
  const pa = PNG.sync.read(a);
  const pb = PNG.sync.read(b);
  return pixelmatch(pa.data, pb.data, null, pa.width, pa.height, { threshold: 0.1 });
}

export async function wearInteractions({ page, openStory, stableStage, stageChangesFrom, fail, outDir }) {
  // A flipped control reaches state the story remembers: the toggles drawn after initiallyChecked
  // goes true -> false match a fresh story opened with false.
  {
    const id = "wear-toggle-buttons--default";
    await openStory(id, "?initiallyChecked=false");
    const fresh = await stableStage();
    await openStory(id, "?initiallyChecked=true");
    const checked = await stableStage();
    await page.evaluate(() => {
      delete document.body.dataset.rendered;
    });
    // The switch's input is invisible; a user clicks the track drawn next to it.
    await page.click('[data-tab="controls"]');
    await page.locator('[data-arg="initiallyChecked"] + span').click();
    await page.waitForFunction((sid) => document.body.dataset.rendered === sid, id, { timeout: 10000 });
    const flipped = await stableStage();
    // Anti-aliasing can differ by a few pixels between two plays of one document, so the flipped
    // render must be far closer to the fresh unchecked one than to the checked one it came from.
    const toFresh = differingPixels(flipped, fresh);
    const toChecked = differingPixels(flipped, checked);
    if (differingPixels(fresh, checked) === 0) fail(`${id}: initiallyChecked draws the same either way`);
    else if (toFresh * 4 >= toChecked) {
      fs.writeFileSync(path.join(outDir, `${id}-fresh.png`), fresh);
      fs.writeFileSync(path.join(outDir, `${id}-flipped.png`), flipped);
      fail(`${id}: after initiallyChecked true -> false the toggles still draw their old state`);
    } else console.log(`  ok a flipped initiallyChecked redraws the toggles (${toFresh} vs ${toChecked} px)`);
  }

  // Pressing a button of the group widens it (RemoteButtonGroupScope.animateWidth): the middle
  // button, at the stage's centre, measured on a row above its icon.
  {
    const id = "wear-button-group--default";
    await openStory(id);
    const rest = await stableStage();
    const box = await page.locator("#stageFrame").boundingBox();
    const restPng = PNG.sync.read(rest);
    const cx = Math.round(restPng.width / 2);
    const cy = Math.round(restPng.height / 2 - restPng.height * 0.07);
    const before = runWidth(restPng, cx, cy);
    await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
    await page.mouse.down();
    const moved = await stageChangesFrom(rest);
    const pressed = PNG.sync.read(await stableStage());
    const during = runWidth(pressed, cx, cy);
    await page.mouse.up();
    const released = runWidth(PNG.sync.read(await stableStage()), cx, cy);
    if (!moved || during <= before + 2)
      fail(`${id}: pressing the middle button left its width at ${before}px -> ${during}px`);
    else if (Math.abs(released - before) > 1) fail(`${id}: released at ${released}px, not back to ${before}px`);
    else console.log(`  ok a pressed group button widens (${before} -> ${during} -> ${released}px)`);
  }

  // A tap steps the intro/outro indicator's progress, and the change animates in the player.
  {
    const id = "wear-curved-progress--intro-outro";
    await openStory(id);
    const rest = await stableStage();
    const box = await page.locator("#stageFrame").boundingBox();
    await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
    if (!(await stageChangesFrom(rest))) fail(`${id}: a tap did not step the progress`);
    else console.log("  ok a tap steps the intro/outro progress");
  }

  // At 100% the circular indicator closes into a full ring (remote-material3 1.0.0-alpha16 fixed
  // the gap it left at its start), as the story's description says. At 90% it is open by one gap
  // at the top, which shows the check sees a real gap.
  {
    const id = "wear-circular-progress--default";
    const render = async (progress) =>
      (await openStory(id, `?progress=${progress}`)) === "ok" ? PNG.sync.read(await stableStage()) : null;
    const fullPng = await render(1);
    const openPng = await render(0.9);
    const geometry = fullPng && ringGeometry(fullPng);
    const full = geometry && ringGaps(fullPng, geometry);
    const open = geometry && openPng && ringGaps(openPng, geometry);
    const describe = (runs) => runs.map(([from, to]) => `${from}..${to}°`).join(", ");
    if (!full || !open) fail(`${id}: did not render, or no indicator pixel in ${INDICATOR_RGB}`);
    else if (full.length > 0) fail(`${id}: at progress=1 the ring has gaps at ${describe(full)}`);
    else if (open.length !== 1 || open[0][1] - open[0][0] < 30 || open[0][1] - open[0][0] > 60)
      fail(`${id}: at progress=0.9 expected one gap of 30-60°, found ${describe(open)}`);
    else console.log(`  ok circular progress closes at 100% (one gap, ${describe(open)}, at 90%)`);
  }

  // The curved indicators do not join up at 100% with a 360° sweep: the stroke pinches at
  // startAngle (135°), as the curved stories' descriptions say. When an rc-players release brings
  // an AndroidX fix, this fails: update those descriptions and turn this check around.
  {
    const id = "wear-curved-progress--default";
    const png =
      (await openStory(id, "?progress=1&sweepAngle=360")) === "ok" ? PNG.sync.read(await stableStage()) : null;
    const geometry = png && ringGeometry(png);
    if (!geometry) fail(`${id}: did not render, or no indicator pixel in ${INDICATOR_RGB}`);
    else {
      const atStart = strokeWidthAt(png, geometry, 135);
      const opposite = strokeWidthAt(png, geometry, 315);
      if (atStart * 2 >= opposite)
        fail(`${id}: no pinch at startAngle any more (${atStart} vs ${opposite}px): update the curved descriptions`);
      else console.log(`  ok curved progress pinches at startAngle at 100% (${atStart} vs ${opposite}px)`);
    }
  }
}
