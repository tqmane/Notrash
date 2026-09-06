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

最初に、下記「フォント」の2ファイルを用意してください。

```sh
npm ci
npm run dev
```

[ローカルプレビュー](http://localhost:3333)

## フォント

NType / NDotは各自で用意し、ローカルでは次の名前で配置してください。フォント本体はGitとVercelへのソースアップロードから除外しています。

- `public/fonts/ntype82_regular.otf`
- `public/fonts/ndot_55.otf`

Vercelでは、フォントをgzip圧縮したBase64を環境変数へ登録します。`web/` で次を実行すると、対応する値が出力されます。

```sh
node scripts/fonts.mjs encode NTYPE_FONT_GZIP_BASE64
node scripts/fonts.mjs encode NDOT_FONT_GZIP_BASE64
```

Vercelの **Project Settings → Environment Variables** に、上記の変数名と出力された値をそれぞれ登録してください。ProductionとPreviewの両方を対象にし、変更後に再デプロイします。値はソースファイルに貼り付けないでください。

ビルド時にフォントを復元し、サイト自身の `/fonts/…` から読み込みます。外部のフォント配信サービスは使いません。Web表示のため、公開サイトではフォントのURLからファイルを取得できます。

ローカルの既存フォントは削除・上書きしません。環境変数のフォントと一致しない場合はビルドを停止します。

## 内容を更新する

- LP: `src/pages/index.astro`、`src/components/Landing.jsx`、`custom.css`、`tokens.css`
- ガイド: `src/content/docs/docs/*.mdx`
- Docsのナビゲーション・外観: `astro.config.mjs`、`docs.css`
- Docsのレイアウト・部品: `src/components/docs/`
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

`vercel.json` にAstro・`npm ci`・`npm run build`・出力先 `dist` を指定済みです。初回デプロイ前に「フォント」の環境変数を登録してください。以後は、`main` へのpushで本番サイトが更新されます。

本番ビルドをローカルで開くには `npm run build` の後に `npm run preview` を実行します。全文検索はビルド時に生成され、previewと公開サイトで利用できます。
