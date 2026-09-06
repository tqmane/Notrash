# Notrash Web

Nothing風のLPと、Astro / StarlightのDocs / Wiki。Vercelへ静的サイトとして公開します。

| パス | 内容 |
| --- | --- |
| `/` | LP、機能紹介、4配色の外観プレビュー |
| `/docs/overview` | 使い方の入口 |
| `/docs/install` | ダウンロードと導入 |
| `/wiki/essential-recorder` | APKとOS側の実装解説 |

## ローカルで開く

Node.js 24 LTSを使用します。

```sh
npm ci
npm run dev
```

[ローカルプレビュー](http://localhost:3333)

## 内容を更新する

- LP: `src/pages/index.astro`、`src/components/Landing.jsx`、`custom.css`、`tokens.css`
- ガイド: `src/content/docs/docs/*.mdx`
- Docsのナビゲーション・外観: `astro.config.mjs`、`docs.css`
- 404: `src/pages/404.astro`
- 画像・フォント: `public/`
- 解析Wiki: 親プロジェクトの `docs/*.md` を編集し、次を実行

```sh
npm run sync:wiki
npm run check:content
npm run build
```

Wikiの生成先は `src/content/docs/wiki/`。本文は親プロジェクトの `docs/*.md` を編集します。

APKの配布先は[GitHub Releases](https://github.com/tqmane/Notrash/releases/latest)です。

一般向けガイドは手順と必要条件に絞ります。解析対象のバージョン・日付・検証条件はWikiへ記載します。特定のroot取得方法を、Notrash全体の必須条件として扱いません。

## 公開

VercelでGitHubの `tqmane/Notrash` を取り込み、Root Directoryを **`web`**、Production Branchを **`main`** に設定します。

`vercel.json` にAstro・`npm ci`・`npm run build`・出力先 `dist` を指定済みです。初回デプロイ後は、`main` へのpushで本番サイトが更新されます。環境変数は不要です。

本番ビルドをローカルで開くには `npm run build` の後に `npm run preview` を実行します。全文検索はビルド時に生成され、previewと公開サイトで利用できます。
