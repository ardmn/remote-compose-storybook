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

// The ring checks' limits. The circular ring at 100% is closed when its thinnest stroke is at least
// CLOSED_MIN_WIDTH of the median; at 90% its gap spans OPEN_GAP_DEGREES (36° of track plus the
// indicator gap) through TOP, the default start angle. CURVED_START_ANGLE is the curved story's
// default startAngle; MIN_STROKE_PX is the thinnest stroke that counts as the ring found.
const CLOSED_MIN_WIDTH = 0.75;
const OPEN_GAP_DEGREES = [30, 60];
const TOP = 270;
const CURVED_START_ANGLE = 135;
const MIN_STROKE_PX = 4;

// Where the ring checks sample: every SAMPLE_STEP degrees around a ring, and every
// RADIAL_STEP pixels across its stroke.
const SAMPLE_STEP = 0.5;
const RADIAL_STEP = 0.25;

// Whether the pixel at (x, y) of a decoded PNG is the indicator colour; false outside the image.
function isIndicator(png, x, y) {
  if (x < 0 || y < 0 || x >= png.width || y >= png.height) return false;
  const i = (y * png.width + x) * 4;
  return INDICATOR_RGB.every((c, k) => Math.abs(png.data[i + k] - c) < INDICATOR_TOLERANCE);
}

// The centre and centre-line radius of a ring drawn in INDICATOR_RGB: the centre of the bounding
// box of its pixels, and their mean distance from it. null when there are none. Exact only for a
// ring that goes all the way round (closed or pinched): an open one's box is cut short.
function ringGeometry(png) {
  const pixels = [];
  for (let y = 0; y < png.height; y++) {
    for (let x = 0; x < png.width; x++) if (isIndicator(png, x, y)) pixels.push([x, y]);
  }
  if (pixels.length === 0) return null;
  const xs = pixels.map(([x]) => x);
  const ys = pixels.map(([, y]) => y);
  const cx = (Math.min(...xs) + Math.max(...xs)) / 2;
  const cy = (Math.min(...ys) + Math.max(...ys)) / 2;
  const radius = pixels.reduce((sum, [x, y]) => sum + Math.hypot(x - cx, y - cy), 0) / pixels.length;
  return { cx, cy, radius };
}

// The point at `degrees` clockwise from 3 o'clock, `distance` pixels from the ring's centre.
function pointAt({ cx, cy }, degrees, distance) {
  const angle = (degrees * Math.PI) / 180;
  return [Math.round(cx + distance * Math.cos(angle)), Math.round(cy + distance * Math.sin(angle))];
}

// The gaps of a ring with the given geometry: runs of [from, to] degrees, clockwise from 3 o'clock,
// where the ring's centre line has no indicator pixel.
function ringGaps(png, geometry) {
  const steps = 360 / SAMPLE_STEP;
  const missing = Array.from(
    { length: steps },
    (_, step) => !isIndicator(png, ...pointAt(geometry, step * SAMPLE_STEP, geometry.radius)),
  );
  const runs = [];
  for (let step = 0; step < steps; step++) {
    if (!missing[step]) continue;
    const last = runs.at(-1);
    if (last && last[1] === step - 1) last[1] = step;
    else runs.push([step, step]);
  }
  // A run through 0° is one gap, not two.
  if (runs.length > 1 && runs[0][0] === 0 && runs.at(-1)[1] === steps - 1) runs[0][0] = runs.pop()[0] - steps;
  return runs.map(([from, to]) => [from * SAMPLE_STEP, (to + 1) * SAMPLE_STEP]);
}

// The stroke's radial width, in pixels, at `degrees` on a ring with the given geometry: indicator
// pixels along the radius within STROKE_SEARCH of the centre line.
const STROKE_SEARCH = 10;
function strokeWidthAt(png, geometry, degrees) {
  let hits = 0;
  for (let d = geometry.radius - STROKE_SEARCH; d <= geometry.radius + STROKE_SEARCH; d += RADIAL_STEP) {
    if (isIndicator(png, ...pointAt(geometry, degrees, d))) hits++;
  }
  return hits * RADIAL_STEP;
}

// The stroke widths at every SAMPLE_STEP around a ring.
function strokeWidths(png, geometry) {
  return Array.from({ length: 360 / SAMPLE_STEP }, (_, step) => strokeWidthAt(png, geometry, step * SAMPLE_STEP));
}

// The number of pixels that differ between two same-size PNG screenshots.
function differingPixels(a, b) {
  const pa = PNG.sync.read(a);
  const pb = PNG.sync.read(b);
  return pixelmatch(pa.data, pb.data, null, pa.width, pa.height, { threshold: 0.1 });
}

export async function wearChecks({ page, openStory, stableStage, stageChangesFrom, fail, outDir }) {
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

  // Geometry checks below save the stage they judged when they fail, for the CI artifact.
  const capture = async (id, query) => {
    const result = await openStory(id, query);
    return { result, png: result === "ok" ? PNG.sync.read(await stableStage()) : null };
  };
  const save = (id, name, png) => fs.writeFileSync(path.join(outDir, `${id}-${name}.png`), PNG.sync.write(png));
  const describe = (runs) => runs.map(([from, to]) => `${from}..${to}°`).join(", ") || "none";

  // At 100% the circular indicator closes into a full ring of even width (remote-material3
  // 1.0.0-alpha16 fixed the gap it left at its start), as the story's description says. At 90% it
  // is open by one gap through the top, which shows the check sees a real gap.
  {
    const id = "wear-circular-progress--default";
    const full = await capture(id, "?progress=1");
    const open = await capture(id, "?progress=0.9");
    const geometry = full.png && ringGeometry(full.png);
    if (!full.png || !open.png) fail(`${id}: progress=1 ${full.result}, progress=0.9 ${open.result}`);
    else if (!geometry) fail(`${id}: no indicator pixel in ${INDICATOR_RGB}`);
    else {
      const fullGaps = ringGaps(full.png, geometry);
      const widths = strokeWidths(full.png, geometry).sort((x, y) => x - y); // sorted for the median
      const median = widths[widths.length >> 1];
      const openGaps = ringGaps(open.png, geometry);
      const [gapFrom, gapTo] = openGaps[0] ?? [0, 0];
      if (fullGaps.length > 0 || widths[0] < CLOSED_MIN_WIDTH * median) {
        save(id, "progress1", full.png);
        fail(`${id}: at progress=1 gaps ${describe(fullGaps)}, thinnest stroke ${widths[0]} of ${median}px`);
      } else if (
        openGaps.length !== 1 ||
        gapTo - gapFrom < OPEN_GAP_DEGREES[0] ||
        gapTo - gapFrom > OPEN_GAP_DEGREES[1] ||
        !(gapFrom <= TOP && TOP <= gapTo)
      ) {
        save(id, "progress0.9", open.png);
        fail(
          `${id}: at progress=0.9 expected one ${OPEN_GAP_DEGREES.join("-")}° gap through ${TOP}°, found ${describe(openGaps)}`,
        );
      } else console.log(`  ok circular progress closes at 100% (one gap, ${describe(openGaps)}, at 90%)`);
    }
  }

  // The curved indicators do not join up at 100% with a 360° sweep: the ring goes all the way
  // round but the stroke pinches at startAngle, as the curved stories' descriptions say. When an
  // rc-players release brings an AndroidX fix, this fails: update CURVED_PINCH in WearCatalog.kt
  // and turn this check around.
  {
    const id = "wear-curved-progress--default";
    const ring = await capture(id, "?progress=1&sweepAngle=360");
    const geometry = ring.png && ringGeometry(ring.png);
    if (!ring.png) fail(`${id}: progress=1 sweepAngle=360 ${ring.result}`);
    else if (!geometry) fail(`${id}: no indicator pixel in ${INDICATOR_RGB}`);
    else {
      const gaps = ringGaps(ring.png, geometry);
      const atStart = strokeWidthAt(ring.png, geometry, CURVED_START_ANGLE);
      const opposite = strokeWidthAt(ring.png, geometry, CURVED_START_ANGLE + 180);
      if (opposite < MIN_STROKE_PX) {
        save(id, "progress1", ring.png);
        fail(`${id}: measured a ${opposite}px stroke opposite startAngle, so the ring was not found`);
      } else if (gaps.length > 0 || atStart * 2 >= opposite) {
        save(id, "progress1", ring.png);
        fail(
          `${id}: no pinch at startAngle (gaps ${describe(gaps)}, ${atStart} vs ${opposite}px): ` +
            "update CURVED_PINCH in WearCatalog.kt",
        );
      } else console.log(`  ok curved progress pinches at startAngle at 100% (${atStart} vs ${opposite}px)`);
    }
  }
}
