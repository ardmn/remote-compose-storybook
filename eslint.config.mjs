// ESLint for the page shell (web/) and the end-to-end test (e2e/).
// Run from e2e/, where the tooling is installed: npm run lint
import js from "./e2e/node_modules/@eslint/js/src/index.js";
import globals from "./e2e/node_modules/globals/index.js";
import noUnsanitized from "./e2e/node_modules/eslint-plugin-no-unsanitized/index.js";

export default [
  { ignores: ["build/**", "**/node_modules/**", "e2e/out/**"] },
  // An eslint-disable comment that no longer suppresses anything fails the lint (the default only
  // warns).
  { linterOptions: { reportUnusedDisableDirectives: "error" } },
  js.configs.recommended,
  {
    files: ["web/**/*.js"],
    languageOptions: { ecmaVersion: 2022, sourceType: "script", globals: { ...globals.browser } },
    rules: { "no-unused-vars": ["error", { args: "none" }], eqeqeq: ["error", "smart"] },
  },
  // The page builds HTML from story data, the URL and translations. It escapes by default: markup is
  // built with the html`` tag in web/app.js, which escapes every interpolation that is not already
  // SafeHtml, and reaches the DOM only through setHtml(), which throws on anything but SafeHtml.
  // setHtml() holds the page's one innerHTML assignment and the one disable comment for this rule;
  // any other innerHTML, outerHTML, insertAdjacentHTML or document.write with a non-literal value
  // fails the lint, so new markup has to go through setHtml().
  { files: ["web/**/*.js"], ...noUnsanitized.configs.recommended },
  {
    files: ["e2e/**/*.mjs", "eslint.config.mjs"],
    languageOptions: { ecmaVersion: 2024, sourceType: "module", globals: { ...globals.node, ...globals.browser } },
    rules: { eqeqeq: ["error", "smart"] },
  },
];
