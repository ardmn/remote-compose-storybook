// Interaction checks of the Wear Material 3 stories: what a tap or a control change must do to
// what the player draws. Called from run.mjs with the page laid out "topstart".
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

// The angles, in degrees out of 360, around a ring drawn in the light indicator colour (the
// lavender primary of the default theme) where no indicator pixel lies on the ring's centre line:
// the gaps of an open ring. The ring's centre and radius come from the indicator pixels themselves.
function ringGaps(png) {
  const isIndicator = (i) => png.data[i] > 170 && png.data[i + 2] > 200 && png.data[i + 2] > png.data[i + 1] + 10;
  const pixels = [];
  for (let y = 0; y < png.height; y++) {
    for (let x = 0; x < png.width; x++) if (isIndicator((y * png.width + x) * 4)) pixels.push([x, y]);
  }
  if (pixels.length === 0) return [0, 360];
  const cx = pixels.reduce((sum, [x]) => sum + x, 0) / pixels.length;
  const cy = pixels.reduce((sum, [, y]) => sum + y, 0) / pixels.length;
  const radius = pixels.reduce((sum, [x, y]) => sum + Math.hypot(x - cx, y - cy), 0) / pixels.length;
  const gaps = [];
  for (let step = 0; step < 720; step++) {
    const angle = (step / 720) * 2 * Math.PI;
    const x = Math.round(cx + radius * Math.cos(angle));
    const y = Math.round(cy + radius * Math.sin(angle));
    if (!isIndicator((y * png.width + x) * 4)) gaps.push(step / 2);
  }
  return gaps;
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
  // the gap it left at its start), and the story's description says so. At 90% the ring is open.
  {
    const id = "wear-circular-progress--default";
    await openStory(id, "?progress=1");
    const full = ringGaps(PNG.sync.read(await stableStage()));
    await openStory(id, "?progress=0.9");
    const open = ringGaps(PNG.sync.read(await stableStage()));
    if (full.length > 0) fail(`${id}: at progress=1 the ring has gaps at ${full.slice(0, 5).join(", ")}°`);
    else if (open.length === 0) fail(`${id}: at progress=0.9 the ring check found no gap`);
    else console.log(`  ok circular progress closes at 100% (open over ${open.length / 2}° at 90%)`);
  }
}
