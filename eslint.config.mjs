// ESLint for the page shell (web/) and the end-to-end test (e2e/).
// Run from e2e/, where the tooling is installed: npm run lint
import js from "./e2e/node_modules/@eslint/js/src/index.js";
import globals from "./e2e/node_modules/globals/index.js";
import noUnsanitized from "./e2e/node_modules/eslint-plugin-no-unsanitized/index.js";

export default [
  { ignores: ["build/**", "**/node_modules/**", "e2e/out/**"] },
  js.configs.recommended,
  {
    files: ["web/**/*.js"],
    languageOptions: { ecmaVersion: 2022, sourceType: "script", globals: { ...globals.browser } },
    rules: { "no-unused-vars": ["error", { args: "none" }], eqeqeq: ["error", "smart"] },
  },
  // The page builds HTML from story data and translations: every innerHTML, insertAdjacentHTML or
  // document.write with a non-literal value is an XSS sink to review. The reviewed ones are in
  // eslint-suppressions.json.
  { files: ["web/**/*.js"], ...noUnsanitized.configs.recommended },
  {
    files: ["e2e/**/*.mjs", "eslint.config.mjs"],
    languageOptions: { ecmaVersion: 2024, sourceType: "module", globals: { ...globals.node, ...globals.browser } },
    rules: { eqeqeq: ["error", "smart"] },
  },
];
