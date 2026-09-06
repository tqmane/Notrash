import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';

export default defineConfig({
  output: 'static',
  trailingSlash: 'never',
  integrations: [
    react(),
    starlight({
      title: 'Notrash',
      description: 'Nothing OS を、自分の設定で。Notrashの使い方と実装をまとめたDocs / Wiki。',
      locales: { root: { label: '日本語', lang: 'ja' } },
      logo: {
        light: './public/images/wordmark-dark.svg',
        dark: './public/images/wordmark-light.svg',
        replacesTitle: true,
      },
      favicon: '/images/favicon.svg',
      customCss: ['./docs.css'],
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/tqmane/Notrash' }],
      disable404Route: true,
      credits: false,
      sidebar: [
        { label: 'ホーム', link: '/' },
        { label: 'Notrash', items: ['docs/overview'] },
        { label: 'はじめる', items: ['docs/install', 'docs/compatibility'] },
        { label: '使い方', items: ['docs/camera', 'docs/essential-voice', 'docs/appearance'] },
        { label: '困ったとき', items: ['docs/troubleshooting'] },
        { label: 'Wiki · 実装を読む', items: ['wiki/camera', 'wiki/essential-recorder', 'wiki/essential-space'] },
      ],
    }),
  ],
});
