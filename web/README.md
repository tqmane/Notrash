# Notrash Web

Nothing風のLPと、MintlifyのDocs / Wiki。

| パス | 内容 |
| --- | --- |
| `/` | LP、機能紹介、4配色の外観プレビュー |
| `/docs/overview` | 使い方の入口 |
| `/docs/install` | ダウンロードと導入 |
| `/wiki/essential-recorder` | APKとOS側の実装解説 |

## ローカルで開く

Node.js 24 LTSを使用します。Mintlify CLIはNode 25に対応していません。

```sh
npm ci
npm run dev
```

[ローカルプレビュー](http://localhost:3333)

## 内容を更新する

- LP: `index.mdx`、`snippets/landing.jsx`、`custom.css`、`tokens.css`
- ガイド: `docs/*.mdx`
- 解析Wiki: 親プロジェクトの `docs/*.md` を編集し、次を実行

```sh
npm run sync:wiki
npm run check:content
npm run validate
npm run check:links
```

JSXの変更がローカルプレビューに反映されない場合は、開発サーバーを再起動してください。

APKの配布先は[GitHub Releases](https://github.com/tqmane/Notrash/releases/latest)です。

一般向けガイドは手順と必要条件に絞ります。解析対象のバージョン・日付・検証条件はWikiへ記載します。特定のroot取得方法を、Notrash全体の必須条件として扱いません。

## 公開

Mintlifyでリポジトリを接続し、`docs.json` を含むこのディレクトリをサイトのルートに指定します。ドメインと公開先の設定はMintlify側で行います。

この構成はローカル検証用に準備しています。公開URLはまだ設定していません。Mintlify標準検索をローカルで使うには `mint login` が必要です。
