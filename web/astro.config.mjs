import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';

export default defineConfig({
  site: 'https://notrash.tqmane.dev',
  output: 'static',
  trailingSlash: 'never',
  integrations: [
    react(),
    starlight({
      title: 'Notrash',
      titleDelimiter: '-',
      description: 'Nothing OS を、自分の設定で。Notrashの使い方と実装をまとめたDocs / Wiki。',
      locales: { root: { label: '日本語', lang: 'ja' } },
      logo: {
        light: './public/images/wordmark-dark.svg',
        dark: './public/images/wordmark-light.svg',
        replacesTitle: true,
      },
      favicon: '/images/favicon.svg',
      customCss: ['./docs.css'],
      components: {
        PageFrame: './src/components/docs/Frame.astro',
        Header: './src/components/docs/Header.astro',
        Sidebar: './src/components/docs/Sidebar.astro',
        TwoColumnContent: './src/components/docs/Columns.astro',
        PageTitle: './src/components/docs/Title.astro',
        Footer: './src/components/docs/Pagination.astro',
        ThemeProvider: './src/components/docs/ThemeProvider.astro',
      },
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/tqmane/Notrash' }],
      disable404Route: true,
      credits: false,
      sidebar: [
        { label: 'Notrash', items: [{ label: 'Notrash — Nothing OS を、自分の設定で。', link: '/' }, 'docs/overview'] },
        { label: 'はじめる', items: ['docs/install', 'docs/compatibility'] },
        { label: '使い方', items: ['docs/camera', 'docs/essential-voice', 'docs/appearance'] },
        { label: '困ったとき', items: ['docs/troubleshooting'] },
        { label: 'Wiki · 実装を読む', items: ['wiki/camera', 'wiki/essential-recorder', 'wiki/essential-space'] },
      ],
    }),
  ],
});
