// Translations with markup must go through tHtml() in web/app.js: t() and data-i18n escape their
// text, so a tag added to a plain key would show up literally. This fails the lint when a key used
// as plain text contains markup in any language, or a tHtml() key has none (it should then be t()).
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";

const web = new URL("../web/", import.meta.url);
const read = (name) => readFileSync(new URL(name, web), "utf8");

const sandbox = {
  URLSearchParams,
  navigator: { language: "en", languages: ["en"] },
  location: { search: "", hash: "" },
  localStorage: { getItem: () => null, setItem() {} },
  document: { documentElement: {} },
};
sandbox.window = sandbox;
runInNewContext(read("i18n.js"), sandbox);
const { en, ru } = sandbox.RcI18n.keys;

const keysOf = (source, pattern) => new Set([...source.matchAll(pattern)].map((m) => m[1]));
const app = read("app.js");
const plain = new Set([
  ...keysOf(app, /\bt\("([\w.]+)"/g),
  ...keysOf(read("index.html"), /data-i18n(?:-[\w-]+)?="([\w.]+)"/g),
]);
const markup = keysOf(app, /\btHtml\("([\w.]+)"/g);
const hasMarkup = (key) => [en, ru].some((dict) => /[<&]/.test(dict[key] ?? ""));

const problems = [
  ...[...plain].filter(hasMarkup).map((k) => `${k}: has markup but is rendered as plain text; use tHtml()`),
  ...[...markup].filter((k) => !hasMarkup(k)).map((k) => `${k}: rendered with tHtml() but has no markup; use t()`),
  ...[...plain, ...markup].filter((k) => !(k in en)).map((k) => `${k}: missing in the English translations`),
];
if (problems.length) {
  console.error(problems.join("\n"));
  process.exit(1);
}
console.log(`i18n markup: ${plain.size} plain keys, ${markup.size} markup keys ok`);
