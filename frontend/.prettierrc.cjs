module.exports = {
    arrowParens: "always",
    bracketSpacing: true,
    jsxSingleQuote: false,
    quoteProps: "as-needed",
    singleQuote: false,
    semi: true,
    printWidth: 100,
    useTabs: false,
    tabWidth: 2,
    trailingComma: "all",
    // https://github.com/trivago/prettier-plugin-sort-imports/issues/131#issuecomment-1163488213
    plugins: [require.resolve("@trivago/prettier-plugin-sort-imports")],
    importOrder: [
      "^react$",
      "@assets/(.*)$",
      "@components/(.*)$",
      "@constants/(.*)$",
      "@hooks/(.*)$",
      "^[./]",
      ".*",
    ],
  };
  