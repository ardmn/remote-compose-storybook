// Link previews (Telegram, LinkedIn, Slack, X, ...). Their crawlers read Open Graph tags from the
// HTML the server returns: they run no JavaScript and never send the part of a URL after "#", so
// every "#/story/..." link would show the same site card. For each story this writes
//   s/<story-id>/index.html  its own title, description and image; a browser is sent on to the story
//   s/<story-id>/card.jpg    1200x630: the component, what it is for, and the story's stage
// plus the site card (social.jpg) and Open Graph tags in index.html, and sets "sharePages" in
// data/meta.json so "Copy link" hands out the s/<id>/ address. The share page is also the story's
// static page for search engines (pages.mjs): s/<id>/ and s/<id>/ru/, with sitemap.xml and llms.txt.
//
//   node e2e/social.mjs [--site build/site]
// Absolute URLs (crawlers need them) use SITE_URL, or the GitHub Pages address of the repository.
import { chromium } from "playwright";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { serveSite, stageTools } from "./lib.mjs";
import { SITE_NAME, esc, llmsTxt, pagePath, sitemap, staticCatalog, storyPage } from "./pages.mjs";

const here = path.dirname(fileURLToPath(import.meta.url));
const args = process.argv.slice(2);
const i = args.indexOf("--site");
const site = path.resolve(here, "..", i >= 0 ? args[i + 1] : "build/site");
const metaFile = path.join(site, "data", "meta.json");
const meta = JSON.parse(fs.readFileSync(metaFile, "utf8"));

function pagesUrl(repository) {
  const m = /^https:\/\/github\.com\/([^/]+)\/([^/]+?)\/?$/.exec(repository || "");
  return m ? `https://${m[1].toLowerCase()}.github.io/${m[2]}/` : "";
}
const siteUrl = (process.env.SITE_URL || pagesUrl(meta.repository)).replace(/\/?$/, "/");
if (siteUrl === "/") {
  console.error("social: set SITE_URL, or build from a GitHub checkout so meta.json names the repository");
  process.exit(1);
}
// Crawlers cache an image by its URL: a new build gets new image URLs (Pages ignores the query).
const version = encodeURIComponent(meta.builtFrom || Date.now().toString(36));
const today = new Date().toISOString().slice(0, 10);
const sources = JSON.parse(fs.readFileSync(path.join(site, "data", "sources.json"), "utf8"));
const api = JSON.parse(fs.readFileSync(path.join(site, "data", "api.json"), "utf8"));

function ogTags({ title, description, url, image, alt, locale = "en_US" }) {
  return [
    `<meta property="og:type" content="website" />`,
    `<meta property="og:locale" content="${locale}" />`,
    `<meta property="og:site_name" content="${SITE_NAME}" />`,
    `<meta property="og:title" content="${esc(title)}" />`,
    `<meta property="og:description" content="${esc(description)}" />`,
    `<meta property="og:url" content="${esc(url)}" />`,
    `<meta property="og:image" content="${esc(image)}" />`,
    `<meta property="og:image:type" content="image/jpeg" />`,
    `<meta property="og:image:width" content="1200" />`,
    `<meta property="og:image:height" content="630" />`,
    `<meta property="og:image:alt" content="${esc(alt)}" />`,
    `<meta name="twitter:card" content="summary_large_image" />`,
    `<meta name="twitter:title" content="${esc(title)}" />`,
    `<meta name="twitter:description" content="${esc(description)}" />`,
    `<meta name="twitter:image" content="${esc(image)}" />`,
  ].join("\n    ");
}

// No <meta http-equiv="refresh">: some crawlers follow it and would read index.html's tags instead.
// The page (pages.mjs) sends a browser on to the live story only when the link carries a query.
const CARD_STYLE = `
  @font-face { font-family: "Roboto Flex"; src: url("fonts/RobotoFlex.ttf"); font-weight: 100 1000; }
  @font-face { font-family: "Noto Sans Mono"; src: url("fonts/NotoSansMono-Variable.ttf"); font-weight: 100 900; }
  * { box-sizing: border-box; }
  html, body { margin: 0; width: 1200px; height: 630px; overflow: hidden; }
  body { font-family: "Roboto Flex", sans-serif; background: #f6f7f9; color: #1b1f24;
         display: flex; gap: 48px; padding: 56px 56px 56px 64px; }
  .text { flex: 1; min-width: 0; display: flex; flex-direction: column; }
  .brand { display: flex; align-items: center; gap: 14px; font-size: 26px; color: #3d4652; }
  .brand img { width: 40px; height: 40px; }
  .brand b { color: #1b1f24; }
  .crumb { margin-top: 44px; font-size: 22px; color: #5f6873; }
  h1 { margin: 6px 0 0; font-size: 56px; line-height: 1.05; font-weight: 750; letter-spacing: -0.01em;
       overflow-wrap: anywhere; }
  h2 { margin: 6px 0 0; font-size: 32px; font-weight: 500; color: #2a5bc4; }
  p { margin: 24px 0 0; font-size: 25px; line-height: 1.4; color: #3d4652;
      display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; overflow: hidden; }
  .foot { margin-top: auto; font-size: 20px; color: #5f6873; }
  .foot code { font: 600 20px "Noto Sans Mono", monospace; color: #2a5bc4; }
  .stage { width: 470px; flex: none; border-radius: 22px; border: 1px solid #dde1e6;
           background: #fff repeating-conic-gradient(#f0f2f5 0 25%, #fff 0 50%) 0 0 / 24px 24px;
           display: flex; align-items: center; justify-content: center; padding: 26px; }
  .stage img { max-width: 100%; max-height: 100%; object-fit: contain; border-radius: 6px;
               box-shadow: 0 6px 24px rgba(20, 30, 50, 0.14); }
  .grid { width: 520px; flex: none; display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .grid .stage { width: auto; padding: 14px; border-radius: 16px; }
`;

function storyCard({ crumb, title, subtitle, description, shot }) {
  return `<!doctype html><html><head><meta charset="utf-8"><style>${CARD_STYLE}</style></head><body>
    <div class="text">
      <div class="brand"><img src="icon.svg" alt="" /><span>Remote Compose <b>Storybook</b></span></div>
      <div class="crumb">${esc(crumb)}</div>
      <h1>${esc(title)}</h1>
      ${subtitle ? `<h2>${esc(subtitle)}</h2>` : ""}
      <p>${esc(description)}</p>
      <div class="foot">Live demo, code and docs for <code>@RemoteComposable</code></div>
    </div>
    <div class="stage"><img src="data:image/png;base64,${shot.toString("base64")}" alt="" /></div>
  </body></html>`;
}

function siteCard({ description, shots, stats }) {
  return `<!doctype html><html><head><meta charset="utf-8"><style>${CARD_STYLE}
    h1 { font-size: 64px; margin-top: 56px; } .crumb { margin-top: 18px; }</style></head><body>
    <div class="text">
      <div class="brand"><img src="icon.svg" alt="" /><span>Jetpack Remote Compose</span></div>
      <h1>Remote Compose Storybook</h1>
      <p>${esc(description)}</p>
      <div class="foot">${esc(stats)}</div>
    </div>
    <div class="grid">${shots
      .map((s) => `<div class="stage"><img src="data:image/png;base64,${s.toString("base64")}" alt="" /></div>`)
      .join("")}</div>
  </body></html>`;
}

const { base, close } = await serveSite(site);
const browser = await chromium.launch();
// Twice the pixels: a 360px stage is drawn larger than that on the card.
const appContext = await browser.newContext({ viewport: { width: 1360, height: 1000 }, deviceScaleFactor: 2 });
const page = await appContext.newPage();
const cardPage = await browser.newPage({ viewport: { width: 1200, height: 630 } });
const { stableStage, localArgs, openStory } = stageTools(page, base);

await page.goto(base);
await page.waitForFunction(() => !!window.rcStorybook, null, { timeout: 60000 });
await page.waitForFunction(() => document.body.dataset.fonts === "ready", null, { timeout: 60000 });
const catalog = JSON.parse(await page.evaluate(() => window.rcStorybook.catalog()));
// The page's own Russian words for each component (the app switches them with its language).
const russian = await page.evaluate((components) => {
  const I18n = window.RcI18n;
  const saved = I18n.lang;
  I18n.lang = "ru";
  const out = {};
  for (const c of components)
    out[c.id] = {
      plain: I18n.plain(c),
      how: I18n.how(c),
      category: I18n.category(c.category),
      stories: Object.fromEntries(c.stories.map((s) => [s.id, I18n.story(s)])),
    };
  I18n.lang = "en";
  for (const c of components) out[c.id].howEn = I18n.how(c);
  I18n.lang = saved;
  return out;
}, catalog.components);
const highlight = (code) => page.evaluate((c) => window.RcHighlight.kotlin(c), code);
const pngSize = (buf) => ({ width: buf.readUInt32BE(16), height: buf.readUInt32BE(20) });

// The card is served from the site's own origin, so it loads the site's font and icon.
let cardHtml = "";
await cardPage.route(base + "_card.html", (route) => route.fulfill({ contentType: "text/html", body: cardHtml }));
async function renderCard(html, file) {
  cardHtml = html;
  await cardPage.goto(base + "_card.html", { waitUntil: "load" });
  await cardPage.evaluate(() => document.fonts.ready);
  await cardPage.screenshot({ path: file, type: "jpeg", quality: 88 });
}

const failures = [];
const recipeShots = [];
let written = 0;
for (const component of catalog.components) {
  for (const story of component.stories) {
    const result = await openStory(story.id, localArgs(story));
    if (result !== "ok") {
      failures.push(`${story.id}: ${result}`);
      continue;
    }
    // A stage that keeps moving (a clock) still makes a card: its frame when time ran out.
    const shot = (await stableStage(4000)).png;
    if (story.id.startsWith("recipe-") && recipeShots.length < 4 && story === component.stories[0]) {
      recipeShots.push(shot);
    }
    const what = component.plain?.what || story.description || "";
    const multiple = component.stories.length > 1;
    const title = multiple ? `${component.title} · ${story.name}` : component.title;
    const dir = path.join(site, "s", story.id);
    fs.mkdirSync(dir, { recursive: true });
    await renderCard(
      storyCard({
        crumb: component.category,
        title: component.title,
        subtitle: multiple ? story.name : "",
        description: what,
        shot,
      }),
      path.join(dir, "card.jpg"),
    );
    const description = story.description && story.description !== what ? `${what} ${story.description}` : what;
    // The stage at its size in CSS pixels: the shot has twice the pixels.
    fs.writeFileSync(path.join(dir, "stage.png"), shot);
    const px = pngSize(shot);
    const src = sources[story.function];
    const code = src ? src.code : "";
    const common = {
      id: story.id,
      siteUrl,
      repository: meta.repository,
      title,
      storyName: story.name,
      multiple,
      componentTitle: component.title,
      description: story.description || "",
      code,
      codeHtml: await highlight(code),
      signatures: component.symbols.map((sym) => ({
        symbol: sym.replace(/^androidx\.compose\.remote\./, ""),
        lines: api[sym] || [],
      })),
      packages: [...new Set(component.symbols.map((x) => x.replace(/\.[^.]+$/, "")))],
      siblings: component.stories.filter((x) => x !== story).map((x) => ({ id: x.id, name: x.name })),
      related: catalog.components
        .filter((c) => c.category === component.category && c !== component && c.stories.length)
        .map((c) => ({ id: c.stories[0].id, title: c.title })),
      image: `${siteUrl}s/${story.id}/stage.png`,
      card: `${siteUrl}s/${story.id}/card.jpg?v=${version}`,
      imageSize: { width: Math.round(px.width / 2), height: Math.round(px.height / 2) },
      dateModified: today,
    };
    const ru = russian[component.id];
    const ruStory = ru.stories[story.id];
    const ruTitle = multiple ? `${component.title} · ${ruStory.name}` : component.title;
    const ruWhat = ru.plain?.what || ruStory.description || "";
    const ruDescription =
      ruStory.description && ruStory.description !== ruWhat ? `${ruWhat} ${ruStory.description}` : ruWhat;
    const tags = (lang) =>
      ogTags({
        title: lang === "ru" ? ruTitle : title,
        description: lang === "ru" ? ruDescription : description,
        url: siteUrl + pagePath(story.id, lang),
        image: common.card,
        alt: `${title}: a live Remote Compose document`,
        locale: lang === "ru" ? "ru_RU" : "en_US",
      });
    fs.writeFileSync(
      path.join(dir, "index.html"),
      storyPage({
        ...common,
        lang: "en",
        category: component.category,
        plain: component.plain || { what, useWhen: [] },
        how: russian[component.id].howEn,
        ogTags: tags("en"),
      }),
    );
    fs.mkdirSync(path.join(dir, "ru"), { recursive: true });
    fs.writeFileSync(
      path.join(dir, "ru", "index.html"),
      storyPage({
        ...common,
        lang: "ru",
        title: ruTitle,
        storyName: ruStory.name,
        description: ruStory.description || "",
        siblings: common.siblings.map((x) => ({ id: x.id, name: ru.stories[x.id].name })),
        category: ru.category,
        plain: ru.plain || component.plain || { what, useWhen: [] },
        how: ru.how,
        ogTags: tags("ru"),
      }).replace(/src="stage\.png"/, 'src="../stage.png"'),
    );
    written++;
  }
}

const storyCount = catalog.components.reduce((n, c) => n + c.stories.length, 0);
const description =
  "Every public @RemoteComposable building block of Jetpack Remote Compose: live, with its code, docs and the document it produces.";
await renderCard(
  siteCard({
    description,
    shots: recipeShots,
    stats: `${catalog.components.length} components · ${storyCount} stories`,
  }),
  path.join(site, "social.jpg"),
);
const indexFile = path.join(site, "index.html");
const tags = ogTags({
  title: SITE_NAME,
  description,
  url: siteUrl,
  image: `${siteUrl}social.jpg?v=${version}`,
  alt: "Remote Compose Storybook: recipes built from Remote Compose blocks",
});
// Site ownership for Google Search Console, Bing Webmaster Tools and Yandex Webmaster: repository
// variables (Settings → Secrets and variables → Actions → Variables), set only once each is created.
const verification = [
  ["google-site-verification", process.env.GOOGLE_SITE_VERIFICATION],
  ["msvalidate.01", process.env.BING_SITE_VERIFICATION],
  ["yandex-verification", process.env.YANDEX_VERIFICATION],
]
  .filter(([, v]) => v)
  .map(([name, v]) => `<meta name="${name}" content="${esc(v)}" />`);
const statics = staticCatalog({ components: catalog.components, description, siteUrl });
const head = [
  ...verification,
  tags,
  `<link rel="canonical" href="${esc(siteUrl)}" />`,
  `<link rel="sitemap" type="application/xml" href="sitemap.xml" />`,
  statics.jsonLd,
];
const index = fs
  .readFileSync(indexFile, "utf8")
  .replace(/\n {4}<!-- og -->[\s\S]*<!-- \/og -->/, "")
  .replace("</head>", `    <!-- og -->\n    ${head.join("\n    ")}\n    <!-- /og -->\n  </head>`)
  // Crawlers that do not run the app still find every component; the app replaces this on start.
  .replace(
    /(<section class="page" id="page">)[\s\S]*?(<\/section>)/,
    (_, open, end) => `${open}\n          ${statics.html}\n        ${end}`,
  );
if (!index.includes(statics.html)) {
  console.error('social: index.html has no <section class="page" id="page"> for the static catalog');
  process.exit(1);
}
fs.writeFileSync(indexFile, index);
const ids = catalog.components.flatMap((c) => c.stories.map((x) => x.id));
fs.writeFileSync(path.join(site, "sitemap.xml"), sitemap({ siteUrl, ids, date: today }));
fs.writeFileSync(
  path.join(site, "llms.txt"),
  llmsTxt({ siteUrl, repository: meta.repository, components: catalog.components, description }),
);
fs.writeFileSync(metaFile, JSON.stringify({ ...meta, sharePages: true }, null, 2));

await browser.close();
close();
console.log(
  `${written} story pages (English and Russian) with preview cards, the site card, sitemap.xml and llms.txt for ${siteUrl}`,
);
if (failures.length) {
  console.log(failures.map((f) => "  FAIL " + f).join("\n"));
  process.exit(1);
}
