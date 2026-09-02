import { defineConfig } from "vite";

export default defineConfig({
    root: "./",
    build: {
        outDir: "./dist",
        rollupOptions: {
            input: "./public/index.html",
            output: {
                entryFileNames: "assets/[name]-[hash].js",
                chunkFileNames: "assets/[name]-[hash].js",
                assetFileNames: "assets/[name]-[hash].[ext]",
            },
        },
    },
    server: {
        port: 3000,
        proxy: {
            "/api": "http://localhost:8080",
        },
    },
});
