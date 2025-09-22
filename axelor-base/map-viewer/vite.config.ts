import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import dns from "dns";

dns.setDefaultResultOrder("verbatim");

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const context = (env.VITE_PROXY_CONTEXT ?? "/").replace(/\/?$/, "/");

  return {
    base: "./",
    build: {
      outDir: "dist",
      chunkSizeWarningLimit: 4000,
      rollupOptions: {
        input: {
          main: path.resolve(__dirname, "index.html"),
        },
      },
    },
    plugins: [react()],
    server: {
      proxy: {
        [path.join(context, "ws")]: {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          ws: true,
        },
        [path.join(context, "js")]: {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          ws: true,
        },
      },
    },
  };
});
