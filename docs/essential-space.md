# Essential Space 2.1.7 の解析

`com.nothing.ntessentialspace` は Essential Space の画面に加え、共通の AI サービス実装も含む。今回の Essential Voice の設定画面・音声入力サービス本体は、別パッケージの [Essential Recorder](essential-recorder.md) にある。

## 解析対象

| 項目 | 値 |
| --- | --- |
| 配布ファイル | `apk/com.nothing.ntessentialspace_2.1.7.apks` |
| versionCode | `540` |
| 最小 / 対象 Android API | `34 / 37` |
| APKS の SHA-256 | `d3117cdb5220ed22d250fd8f6e45031889d2294f858c31e889d107778379c081` |

解析日・実機確認日: 2026-09-06。実機では 2.1.7 が有効。`dumpsys package` には無効化されたプリインストール版 2.0.7 の情報も出るため、最初の有効パッケージ情報と混同しない。

`.apks` は単一 APK ではなく、複数 APK のコンテナ。今回の `apk/extracted_space/` には次の4つがある。

- `base.apk`
- `split_config.arm64_v8a.apk`
- `split_config.ja.apk`
- `split_config.xxhdpi.apk`

更新時は機種・言語・密度に適合する split を含めて扱う。Voice の解析のために Space の APK を改変する必要はなかった。

## 主な構成

| クラス | 役割 |
| --- | --- |
| `com.nothing.ntessentialspace.AIEntryActivity` | 純正 Essential 一覧から Space へ入る画面 |
| `com.nothing.ntessentialspace.MainActivity` | Space 本体の画面 |
| `com.nothing.ai.service.AiService` | AI リクエストとコールバックを扱うサービス |
| `com.nothing.ai.service.EngineManager` | エンジンの生成・キャッシュ・解放 |
| `com.nothing.ai.service.sdk.EngineType` | `SCREENSHOT` / `ASR` / `FLIP_2_RECORD` の種別 |
| `com.nothing.ai.service.engine.SharedAsrEngine` | ASR エンジンの実装 |

`com.nothing.ai.service` はここではクラスの名前空間でもある。この名前のクラスがあることと、同名の独立 APK がインストールされていることは別。

`AIEntryActivity` のボタンは `MainActivity` を起動する。Voice の `EssentialVoiceIntroActivity` とは異なる入口。

## 機種判定と、旧 Notrash の問題

このバージョンには次の機種判定がある。

- `defpackage.xh3.a(String)`: Nothing の機能フラグを参照する。
- `defpackage.kz5.y()`: `NTF_ASTEROIDS` を参照する。
- `defpackage.kz5.z()`: `NTF_ASTEROIDS_PLUS` を参照する。
- `com.nothing.ai.service.engine.util.Utils.isAsteroidsSeries()`: 上記2つのフラグの OR。
- `EngineManager.getAsrEngine`: 機種判定から ASR エンジンを選択する。

旧 Notrash は Voice 設定を ON にしたとき、これらの機種判定も広く true にしていた。しかし、それでは Space 内のエンジン選択などにも影響する。キーボードの Voice ボタンを制御するのは OS の `NtEssentialVoiceImpl` であり、これらを true にしても OS の拡張サービスは開放されない。

修正版では `NTF_ASTEROIDS` / `NTF_ASTEROIDS_PLUS` / `kz5.y,z` / `isAsteroidsSeries` の偽装を削除した。Space / AI サービスのスコープでは、共通の `NTF_ESSENTIAL_VOICE` 対応判定への条件付きフックだけを登録する。スクリーンショット処理や ASR エンジン選択を変更する実装はない。

## Voice との関係を判断する根拠

- Voice の Intro / Settings / Tutorial / Service は Recorder の manifest にある。
- Recorder 自身に `NetworkApi.voiceInput` と `essentialVoice/ai/v1/transcription` の定義がある。
- OS の Voice 実装の接続先は `com.nothing.ntessentialrecorder/.EssentialVoiceService`。
- Space の ASR エンジンを偽装せず、A059 で Voice の入口・キーボードボタンが表示された。

これは「Space を削除しても Voice の全機能が動く」と検証したという意味ではない。インストール済みの Space を残した状態で確認しており、パッケージ間のすべての依存関係の分離試験はしていない。

## UI の色とカード

公式掲載画像でも、小さな間隔で配置した角丸カード、通常の本文書体、控えめな区切りを確認できる。実装の `defpackage.cn0`（ComposeUtils）は背景を `R.color.background`、カードを `R.color.card_bg` から取得する。夜間の値は `intelligence_window_background_color` / `intelligence_panel_background_color` を参照する。

同じユーティリティには `extraDarkColor` と `isExtraDark` があり、色リソースの状態で切り替える。Notrash のハイコントラストは他アプリや端末の設定を変えるものではなく、Notrash 内だけで選べる独立した表示設定として実装している。

## 更新時の確認

1. 有効なパッケージのバージョンと、APKS / split の組み合わせを確認する。
2. Voice の問題なら Recorder の入口と OS の `system` スコープを先に確認する。
3. Space のエンジン初期化の問題なら `AiService` / `EngineManager` のログと実装を調べる。
4. 機種判定が存在するという理由だけで、全フラグを true にしない。

Space 全機能の回帰試験、ASR の精度・利用制限・サーバー応答は今回の検証範囲外。

## 解析元

ワークスペース直下の `decompiled_space/sources/`:

- `com/nothing/ntessentialspace/AIEntryActivity.java`
- `com/nothing/ai/service/AiService.java`
- `com/nothing/ai/service/EngineManager.java`
- `com/nothing/ai/service/sdk/EngineType.java`
- `com/nothing/ai/service/engine/util/Utils.java`
- `defpackage/xh3.java`、`defpackage/kz5.java`

Notrash 側: [EssentialVoiceHooks.java](../app/src/main/java/com/notrash/xposed/hooks/EssentialVoiceHooks.java)

[全体の導入手順](../README.md) · [カメラ](camera.md) · [Essential Recorder](essential-recorder.md)
