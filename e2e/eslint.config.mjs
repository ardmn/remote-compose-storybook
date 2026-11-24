// ESLint for the page shell (web/) and the end-to-end test (e2e/). It lives in e2e/, next to the
// package that installs ESLint and its plugins, so the imports below resolve (and knip sees them).
// Run it with npm run lint in e2e/: the script runs ESLint from the repository root with
// --config e2e/eslint.config.mjs, and with --config ESLint resolves the file patterns below against
// the working directory, the repository root.
import js from "@eslint/js";
import globals from "globals";
import noUnsanitized from "eslint-plugin-no-unsanitized";

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
  // fails the lint, so new markup has to go through setHtml(). raw() skips escaping, so every call
  // is reported too: each one carries a disable comment saying why its input is trusted markup.
  { files: ["web/**/*.js"], ...noUnsanitized.configs.recommended },
  {
    files: ["web/app.js"],
    rules: {
      "no-restricted-syntax": [
        "error",
        {
          selector: "CallExpression[callee.name='raw']",
          message: "raw() skips escaping: pass only bundled markup, and say why in a disable comment.",
        },
      ],
    },
  },
  {
    files: ["e2e/**/*.mjs"],
    languageOptions: { ecmaVersion: 2024, sourceType: "module", globals: { ...globals.node, ...globals.browser } },
    rules: { eqeqeq: ["error", "smart"] },
  },
];
