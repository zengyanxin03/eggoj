const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());

    config.plugin("fork-ts-checker").tap((args) => {
      // 禁用语法错误检查
      args[0].typescript = {
        ...args[0].typescript,
        diagnosticOptions: {
          ...args[0].typescript?.diagnosticOptions,
          syntactic: false,
          semantic: true, // 保留语义检查
        },
      };
      return args;
    });
  },
});
