// The config lives in e2e/ (next to the packages it imports, so knip sees them); this file only lets
// editors and a plain `npx eslint` run from the repo root find it.
export { default } from "./e2e/eslint.config.mjs";
