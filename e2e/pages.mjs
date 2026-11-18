// Static pages for search engines and AI crawlers, written by social.mjs next to the link previews.
// The storybook itself draws everything with JavaScript and Wasm behind "#/story/..." addresses,
// which search engines index as one empty page. So every story also gets a plain HTML page,
//   s/<story-id>/      English
//   s/<story-id>/ru/   Russian (the page's own words; the API, code and story names stay in English)
// with the component's explanation, a picture of its stage, its Kotlin code and API signatures,
// structured data (JSON-LD) and links to its neighbours; plus sitemap.xml and llms.txt, and a static
// list of every component inside index.html for crawlers that do not run the app.
//
// A page opened from a shared link ("Copy link" adds a query: the story's args or "play") sends the
// browser straight on to the live story. Without a query it stays a page, with a button to the live
// demo, so search engines that run JavaScript index the page instead of following a redirect.

export const SITE_NAME = "Remote Compose Storybook";
export const AUTHOR = { "@type": "Person", name: "ardmn", url: "https://github.com/ardmn" };

export const esc = (s) =>
  String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);

// JSON inside <script type="application/ld+json">: "</script>" in a string must not end the element.
const ld = (data) => JSON.stringify(data, null, 1).replace(/</g, "\\u003c");

const WORDS = {
  en: {
    title: (t) => `${t} — Jetpack Remote Compose example`,
    live: "Open the live, interactive demo",
    liveNote: "Change its controls, read the remote document it produces and the diff after every change.",
    what: "What it is",
    use: "Good for",
    how: "How it works",
    code: "Code",
    codeNote:
      "The story's source with <code>androidx.compose.remote</code> imports. CI compiles every snippet on this site against AndroidX.",
    api: "API",
    reference: "Reference",
    stories: "Other stories of this component",
    more: (c) => `More in ${c}`,
    stage: (t) => `${t}: the document drawn by the Remote Compose player`,
    about:
      "Remote Compose lets a server describe a piece of Android app UI as a small document instead of shipping new app code; the app plays it. This catalog shows every public <code>@RemoteComposable</code> building block of <code>androidx.compose.remote</code>, live in the browser.",
    home: "All components",
    source: "Source on GitHub",
    lang: "Русский",
    rn: "Play the same documents in React Native",
  },
  ru: {
    title: (t) => `${t} — пример Jetpack Remote Compose`,
    live: "Открыть живое интерактивное демо",
    liveNote: "Меняйте параметры, смотрите удалённый документ, который получается, и разницу после каждого изменения.",
    what: "Что это",
    use: "Подходит для",
    how: "Как это работает",
    code: "Код",
    codeNote:
      "Исходник примера с импортами <code>androidx.compose.remote</code>. CI компилирует каждый фрагмент кода на сайте против AndroidX.",
    api: "API",
    reference: "Справочник",
    stories: "Другие примеры этого компонента",
    more: (c) => `Ещё в разделе «${c}»`,
    stage: (t) => `${t}: документ, нарисованный плеером Remote Compose`,
    about:
      "Remote Compose позволяет серверу описать часть интерфейса Android-приложения небольшим документом вместо выпуска нового кода приложения; приложение его проигрывает. Этот каталог показывает каждый публичный строительный блок <code>@RemoteComposable</code> из <code>androidx.compose.remote</code> вживую в браузере.",
    home: "Все компоненты",
    source: "Исходники на GitHub",
    lang: "English",
    rn: "Те же документы в React Native",
  },
};

export const RN_URL = "https://github.com/ardmn/react-native-remote-compose";

/** The address of a story's page: s/<id>/ or s/<id>/ru/. */
export const pagePath = (id, lang) => `s/${id}/${lang === "ru" ? "ru/" : ""}`;

/**
 * One story's page. `s` holds everything already in the page's language:
 * {id, lang, siteUrl, repository, title, storyName, multiple, category, componentTitle, description,
 *  plain: {what, useWhen}, how (html or null), codeHtml, code, signatures: [{symbol, lines}],
 *  packages, siblings: [{id, name}], related: [{id, title}], image, card, imageSize, dateModified}
 */
export function storyPage(s) {
  const w = WORDS[s.lang];
  const up = s.lang === "ru" ? "../../../" : "../../";
  const url = s.siteUrl + pagePath(s.id, s.lang);
  const other = s.lang === "ru" ? "en" : "ru";
  const urls = { en: s.siteUrl + pagePath(s.id, "en"), ru: s.siteUrl + pagePath(s.id, "ru") };
  // Back to the app in the same language: the app reads ?lang= once and remembers it.
  const app = s.lang === "ru" ? `${up}?lang=ru` : up;
  const live = `${app}#/story/${s.id}`;
  const fullTitle = w.title(s.title);
  const description = `${s.plain.what} ${s.description}`.trim();
  const crumbs = [
    { name: SITE_NAME, item: s.siteUrl },
    { name: s.componentTitle, item: url },
  ];
  const data = [
    {
      "@context": "https://schema.org",
      "@type": "TechArticle",
      headline: fullTitle,
      description,
      url,
      mainEntityOfPage: url,
      inLanguage: s.lang,
      image: s.card,
      dateModified: s.dateModified,
      author: AUTHOR,
      publisher: AUTHOR,
      proficiencyLevel: "Beginner",
      about: [
        {
          "@type": "Thing",
          name: "Jetpack Remote Compose",
          sameAs: "https://developer.android.com/jetpack/androidx/releases/compose-remote",
        },
        { "@type": "Thing", name: s.componentTitle },
      ],
      isPartOf: { "@type": "WebSite", name: SITE_NAME, url: s.siteUrl },
      hasPart: {
        "@type": "SoftwareSourceCode",
        name: `${s.componentTitle} · ${s.storyName}`,
        programmingLanguage: "Kotlin",
        codeSampleType: "code snippet",
        runtimePlatform: "Android",
        codeRepository: s.repository || undefined,
        text: s.code,
      },
    },
    {
      "@context": "https://schema.org",
      "@type": "BreadcrumbList",
      itemListElement: crumbs.map((c, i) => ({ "@type": "ListItem", position: i + 1, name: c.name, item: c.item })),
    },
  ];
  const signatures = s.signatures
    .map(
      (g) =>
        `<h3><code>${esc(g.symbol)}</code></h3>` +
        (g.lines.length ? `<div class="code-block"><pre><code>${esc(g.lines.join("\n"))}</code></pre></div>` : ""),
    )
    .join("\n");
  const reference = s.packages
    .map(
      (p) =>
        `<li><a href="https://developer.android.com/reference/kotlin/${p.replace(/\./g, "/")}/package-summary"><code>${esc(p)}</code></a></li>`,
    )
    .join("");
  const linkList = (items) => `<ul class="links">${items.join("")}</ul>`;
  return `<!doctype html>
<html lang="${s.lang}">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${esc(fullTitle)} · ${SITE_NAME}</title>
    <meta name="description" content="${esc(description)}" />
    <link rel="canonical" href="${esc(url)}" />
    <link rel="alternate" hreflang="en" href="${esc(urls.en)}" />
    <link rel="alternate" hreflang="ru" href="${esc(urls.ru)}" />
    <link rel="alternate" hreflang="x-default" href="${esc(urls.en)}" />
    <link rel="icon" type="image/svg+xml" href="${up}icon.svg" />
    <link rel="stylesheet" href="${up}styles.css" />
    <link rel="stylesheet" href="${up}page.css" />
    ${s.ogTags}
    <script type="application/ld+json">
${ld(data)}
    </script>
    <script>
      // Opened from a shared link (a query: the story's args, or "play"): go on to the live story.
      (function () {
        var q = new URLSearchParams(location.search);
        if (!location.search) return;
        q.delete("play");
        var rest = q.toString();
        location.replace(${JSON.stringify(live)} + (rest ? "?" + rest : ""));
      })();
    </script>
  </head>
  <body class="static-page">
    <header class="static-top">
      <a class="brand" href="${app}"><span class="brand-mark" aria-hidden="true"></span><span>Remote Compose <b>Storybook</b></span></a>
      <span class="static-spacer"></span>
      <a href="${other === "ru" ? "ru/" : "../"}" hreflang="${other}" lang="${other}">${w.lang}</a>
      ${s.repository ? `<a href="${esc(s.repository)}">GitHub</a>` : ""}
    </header>
    <main class="static-main">
      <nav class="static-crumbs" aria-label="Breadcrumb"><a href="${app}">${SITE_NAME}</a> › ${esc(s.category)} › ${esc(s.componentTitle)}</nav>
      <h1>${esc(s.componentTitle)}${s.multiple ? ` <span class="static-sub">· ${esc(s.storyName)}</span>` : ""}</h1>
      <p class="static-lede">${esc(s.description)}</p>
      <div class="static-hero">
        <figure class="static-stage">
          <img src="stage.png" width="${s.imageSize.width}" height="${s.imageSize.height}" alt="${esc(w.stage(s.title))}" />
        </figure>
        <div class="static-cta">
          <a class="btn primary static-live" href="${live}">${w.live} →</a>
          <p>${w.liveNote}</p>
        </div>
      </div>
      <div class="static-plain">
        <section class="plain-card"><h2>${w.what}</h2><p>${esc(s.plain.what)}</p></section>
        <section class="plain-card"><h2>${w.use}</h2><ul>${s.plain.useWhen.map((u) => `<li>${esc(u)}</li>`).join("")}</ul></section>
      </div>
      ${s.how ? `<section><h2>${w.how}</h2>${s.how}</section>` : ""}
      <section>
        <h2>${w.code}</h2>
        <p class="static-note">${w.codeNote}</p>
        <div class="code-block"><pre><code>${s.codeHtml}</code></pre></div>
      </section>
      <section>
        <h2>${w.api}</h2>
        ${signatures}
        ${reference ? `<h3>${w.reference}</h3><ul class="links">${reference}</ul>` : ""}
      </section>
      ${
        s.siblings.length
          ? `<section><h2>${w.stories}</h2>${linkList(s.siblings.map((x) => `<li><a href="${rel(s.lang, x.id)}">${esc(x.name)}</a></li>`))}</section>`
          : ""
      }
      ${
        s.related.length
          ? `<section><h2>${esc(w.more(s.category))}</h2>${linkList(s.related.map((x) => `<li><a href="${rel(s.lang, x.id)}">${esc(x.title)}</a></li>`))}</section>`
          : ""
      }
    </main>
    <footer class="static-foot">
      <p>${w.about}</p>
      <p><a href="${app}">${w.home}</a>${s.repository ? ` · <a href="${esc(s.repository)}">${w.source}</a>` : ""} · <a href="${RN_URL}">${w.rn}</a></p>
    </footer>
  </body>
</html>
`;
}

// From one story's page to another story's page in the same language.
function rel(lang, id) {
  return lang === "ru" ? `../../${id}/ru/` : `../${id}/`;
}

/** The static catalog put inside index.html: every component, linked to its page. */
export function staticCatalog({ components, description, siteUrl }) {
  const byCategory = new Map();
  for (const c of components) {
    if (!byCategory.has(c.category)) byCategory.set(c.category, []);
    byCategory.get(c.category).push(c);
  }
  let html = `<h1>Remote Compose Storybook: every Jetpack Remote Compose component, live</h1>
          <p>${esc(description)}</p>`;
  for (const [category, list] of byCategory) {
    html += `\n          <h2>${esc(category)}</h2><ul>`;
    for (const c of list) {
      html += `<li><a href="${pagePath(c.stories[0].id, "en")}"><b>${esc(c.title)}</b></a>: ${esc(c.plain?.what || c.description)}</li>`;
    }
    html += "</ul>";
  }
  const data = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: SITE_NAME,
    alternateName: "Jetpack Remote Compose component catalog",
    url: siteUrl,
    description,
    inLanguage: ["en", "ru"],
    author: AUTHOR,
    about: {
      "@type": "Thing",
      name: "Jetpack Remote Compose",
      sameAs: "https://developer.android.com/jetpack/androidx/releases/compose-remote",
    },
  };
  return { html, jsonLd: `<script type="application/ld+json">\n${ld(data)}\n    </script>` };
}

/** sitemap.xml: the site and every story page in both languages, each naming its other language. */
export function sitemap({ siteUrl, ids, date }) {
  const entry = (loc, alternates, priority) =>
    `  <url>\n    <loc>${esc(loc)}</loc>\n    <lastmod>${date}</lastmod>\n    <priority>${priority}</priority>\n${alternates
      .map((a) => `    <xhtml:link rel="alternate" hreflang="${a.lang}" href="${esc(a.href)}" />`)
      .join("\n")}${alternates.length ? "\n" : ""}  </url>`;
  const urls = [entry(siteUrl, [], "1.0")];
  for (const id of ids) {
    const alternates = [
      { lang: "en", href: siteUrl + pagePath(id, "en") },
      { lang: "ru", href: siteUrl + pagePath(id, "ru") },
      { lang: "x-default", href: siteUrl + pagePath(id, "en") },
    ];
    urls.push(entry(siteUrl + pagePath(id, "en"), alternates, "0.8"));
    urls.push(entry(siteUrl + pagePath(id, "ru"), alternates, "0.6"));
  }
  return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">
${urls.join("\n")}
</urlset>
`;
}

/** llms.txt (https://llmstxt.org): what the site is, and a link to every component's page. */
export function llmsTxt({ siteUrl, repository, components, description }) {
  const lines = [
    `# ${SITE_NAME}`,
    "",
    `> ${description}`,
    "",
    "Jetpack Remote Compose (`androidx.compose.remote`) lets a server describe Android UI as a small document " +
      "(`.rc`) that the app plays: layout, drawing, animation and taps run on the device, and the server can change " +
      "the UI without an app release. Each page below explains one public `@RemoteComposable` building block, " +
      "with Kotlin code that CI compiles against AndroidX and the API signatures from `api/current.txt`.",
    "",
  ];
  const byCategory = new Map();
  for (const c of components) {
    if (!byCategory.has(c.category)) byCategory.set(c.category, []);
    byCategory.get(c.category).push(c);
  }
  for (const [category, list] of byCategory) {
    lines.push(`## ${category}`, "");
    for (const c of list) {
      for (const s of c.stories) {
        const title = c.stories.length > 1 ? `${c.title} · ${s.name}` : c.title;
        lines.push(`- [${title}](${siteUrl}${pagePath(s.id, "en")}): ${s.description || c.plain?.what || ""}`);
      }
    }
    lines.push("");
  }
  lines.push("## Optional", "");
  if (repository) lines.push(`- [Source code](${repository}): the stories, the site and its tests`);
  lines.push(
    `- [react-native-remote-compose](${RN_URL}): play Remote Compose documents in React Native on Android and iOS`,
  );
  lines.push(
    "- [AndroidX release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote): Remote Compose versions",
    "",
  );
  return lines.join("\n");
}
