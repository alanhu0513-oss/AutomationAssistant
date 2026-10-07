import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import fs from 'fs';
import path from 'path';

export default defineConfig({
  plugins: [
    react(),
    {
      name: 'apk-mime-middleware',
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          if (req.url && (req.url.endsWith('.apk') || req.url.includes('.apk?'))) {
            const cleanUrl = req.url.split('?')[0];
            const apkPath = path.resolve(__dirname, 'public', cleanUrl.replace(/^\//, ''));
            if (fs.existsSync(apkPath)) {
              res.setHeader('Content-Type', 'application/vnd.android.package-archive');
              res.setHeader('Content-Disposition', `attachment; filename="${path.basename(apkPath)}"`);
              const stat = fs.statSync(apkPath);
              res.setHeader('Content-Length', stat.size);
              const readStream = fs.createReadStream(apkPath);
              return readStream.pipe(res);
            }
          }
          next();
        });
      },
    },
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'icon.svg', 'apple-touch-icon.png'],
      manifest: {
        id: '/',
        name: 'Aegis Shield - Game Window Protection',
        short_name: 'Aegis',
        description: 'Stop surface-level game-time popups before they end your match.',
        theme_color: '#07090E',
        background_color: '#07090E',
        display: 'standalone',
        start_url: '/',
        scope: '/',
        icons: [
          {
            src: '/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: '/pwa-maskable-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
      devOptions: {
        enabled: true,
        type: 'module',
      },
    }),
  ],
  server: {
    host: '0.0.0.0',
    port: 3000,
  },
});
