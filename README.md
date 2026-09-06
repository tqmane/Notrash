# Notrash
[![技術者倫理 遵守済み](https://img.shields.io/badge/%E6%8A%80%E8%A1%93%E8%80%85%E5%80%AB%E7%90%86-%E9%81%B5%E5%AE%88%E6%B8%88%E3%81%BF-0a0a0a?style=for-the-badge&labelColor=ffffff)](https://技術者倫理.com)

Nothing OS 向けの拡張モジュール。使いたい機能をアプリから個別に切り替えられます。

- **カメラ**: シャッター音設定の制限を解除し、マナーモード中も操作可能にします。
- **Essential Voice**: 純正の設定入口とキーボードボタンを利用できるようにします。
- **表示**: システム／ライト／ダークと、2つのコントラストから選べます。

拡張機能は初期OFF。表示はハイコントラストが初期ONです。

## 導入

1. libxposed API 102対応のVector / LSPosedを用意します。
2. [最新リリース](https://github.com/tqmane/Notrash/releases/latest)をインストールし、Vector / LSPosedでNotrashを有効にして推奨スコープを適用します。
3. Notrashで使いたい機能をONにします。カメラは終了して開き直してください。

Nothing Phone (3a)でBootloader Unlockせずに使う方法は、[Root-My-Device v1.0](https://github.com/tqmane/Root-My-Device/releases/tag/v1.0)を参照してください。対象ビルドなどの条件はリンク先に記載されています。

## ドキュメント

- [Webサイト・Docs / Wiki](web/README.md)
- [カメラの解析](docs/camera.md)
- [Essential RecorderとOSのVoice連携](docs/essential-recorder.md)
- [Essential Spaceの解析](docs/essential-space.md)
- [Notrashの内部構成](SPEC.md)

VoiceにはOS側の対応実装が必要です。確認済みの環境と範囲は各解析ページを参照してください。

## ビルドと検証

Android SDK 37、JDK 17以上。

NType / NDotのフォントファイルは含めていません。各自で用意し、ビルド前に次の名前で配置してください。配置したファイルはGitの追跡対象外です。

- `app/src/main/res/font/ntype82_regular.otf`
- `app/src/main/res/font/ndot_55.otf`

Web用フォントの配置とVercelの設定は [WebのREADME](web/README.md#フォント) を参照してください。

```powershell
.\gradlew.bat :app:compileDebugKotlin :app:assembleDebug --no-daemon
python verification/check_system_voice.py
python verification/check_appearance.py
python verification/smoke.py
```

APK: `app/build/outputs/apk/debug/app-debug.apk`。

アイコンの原本は `app/src/main/res/drawable/ic_notrash_foreground.xml`。PNGとクイック設定用の書き出しは `python tools/generate-icons.py` で揃えます。

表示設定は保存されます。「対象アプリを終了」はカメラ・Recorder・Spaceのみを終了し、端末やOSプロセスを再起動しません。

## ライセンス

Copyright (C) 2026 tqmane

Notrashのソースコードは [GNU GPL v3.0](LICENSE)（`GPL-3.0-only`）で提供します。第三者のライブラリ・フォント・画像には、それぞれのライセンスが適用されます。
