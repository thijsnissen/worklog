import { defineConfig } from "eslint/config";
import globals from "globals";
import htmlPlugin from "@html-eslint/eslint-plugin";
import cssPlugin from "@eslint/css";
import tsEslint from "typescript-eslint";
import tsParser from "@typescript-eslint/parser";

export default defineConfig([
    // TypeScript
    ...tsEslint.configs.recommendedTypeChecked.map((conf) => ({
        ...conf,
        files: ["**/*.ts"],
    })),

    {
        files: ["**/*.ts"],
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "module",
            globals: {
                ...globals.browser,
                lucide: "readonly",
            },
            parser: tsParser,
            parserOptions: {
                projectService: true,
            },
        },
        rules: {
            "no-console": "warn",
            "no-debugger": "error",
        },
    },

    // HTML
    {
        files: ["**/*.html"],
        plugins: { html: htmlPlugin },
        language: "html/html",
        rules: {
            "html/no-duplicate-class": "error",
            "html/require-img-alt": "error",
        },
    },

    // CSS
    {
        files: ["**/*.css"],
        plugins: { css: cssPlugin },
        language: "css/css",
        rules: {
            "css/no-duplicate-imports": "error",
            "css/no-empty-blocks": "warn",
        },
    },
    {
        ignores: ["node_modules/**/*", "dist/**/*", "*.config.ts"],
    },
]);
