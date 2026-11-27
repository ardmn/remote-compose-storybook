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
}
