import { dirname } from "path";
import { fileURLToPath } from "url";
import eslintPluginNext from "@next/eslint-plugin-next";
import js from "@eslint/js";
import stylistic from "@stylistic/eslint-plugin";
import importPlugin from "eslint-plugin-import";
import prettier from "eslint-plugin-prettier";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import tseslint from "typescript-eslint";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const eslintIgnore = [
  ".git/",
  ".next/",
  "node_modules/",
  "build/",
  "*.min.js",
  "*.d.ts",
]

export default tseslint.config(
  { ignores: eslintIgnore },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.es2021,
      },

      ecmaVersion: "latest",
      sourceType: "module",

      parser: tseslint.parser,

      parserOptions: {
        projectService: true,
        tsconfigRootDir: __dirname,
      },
    },
    plugins: {
      prettier,
      "import-plugin": importPlugin,
      "@stylistic": stylistic,
      react,
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
      "@typescript-eslint": tseslint.plugin,
      "@next": eslintPluginNext,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      ...eslintPluginNext.configs["core-web-vitals"].rules,
      "react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
      "prettier/prettier": "warn",
      "@typescript-eslint/strict-boolean-expressions": "off",
      "react/react-in-jsx-scope": "off",
      "react/jsx-uses-react": "off",
      "@typescript-eslint/consistent-type-definitions": ["error", "type"],
      "react/jsx-boolean-value": "error",
      "no-nested-ternary": "error",
      "@typescript-eslint/ban-ts-comment": [
        "error",
        {
          "ts-expect-error": "allow-with-description",
          minimumDescriptionLength: 3,
        },
      ],
      "@typescript-eslint/no-unused-vars": [
        "warn",
        { varsIgnorePattern: "^_", argsIgnorePattern: "^_" },
      ],
      "@typescript-eslint/no-misused-promises": [
        "error",
        {
          checksVoidReturn: false,
        },
      ],
      "@typescript-eslint/no-floating-promises": "off",
      "@typescript-eslint/consistent-type-definitions": ["error", "type"],
      "@typescript-eslint/no-duplicate-type-constituents": "warn",
      "@typescript-eslint/no-explicit-any": "error",
      "@typescript-eslint/no-extra-non-null-assertion": "warn",
      "@typescript-eslint/no-for-in-array": "error",
      "no-implied-eval": "off",
      "@typescript-eslint/no-implied-eval": "error",
      "@typescript-eslint/no-invalid-void-type": "warn",
      "@typescript-eslint/no-non-null-asserted-nullish-coalescing": "error",
      "@typescript-eslint/no-non-null-assertion": "warn",
      "default-param-last": "off",
      "@typescript-eslint/default-param-last": "warn",
      "@typescript-eslint/no-array-delete": "error",
      "@typescript-eslint/no-unnecessary-boolean-literal-compare": "warn",
      "@typescript-eslint/no-unnecessary-condition": "error",
      "@typescript-eslint/no-unsafe-assignment": "error",
      "@typescript-eslint/no-unsafe-function-type": "error",
      "@typescript-eslint/no-wrapper-object-types": "error",
      "@typescript-eslint/prefer-find": "warn",
      "@typescript-eslint/prefer-includes": "warn",
      "@typescript-eslint/prefer-optional-chain": "error",
      "prefer-template": "error",
      "@typescript-eslint/no-base-to-string": "error",
      "@typescript-eslint/require-array-sort-compare": "warn",
      "@typescript-eslint/no-unsafe-argument": "error",
      "import-plugin/newline-after-import": "warn",
    },
  },
);
