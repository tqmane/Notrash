# Notrash の内部構成

アプリ別の解析: [カメラ](docs/camera.md) / [Essential Recorder と OS の Voice](docs/essential-recorder.md) / [Essential Space](docs/essential-space.md)。APK のバージョン、SHA-256、解析元と実機確認範囲は各ページに記載。

## 設定共有

[NotrashConfig](app/src/main/java/com/notrash/data/NotrashConfig.kt) がアプリの設定を保存し、libxposed Service の `getRemotePreferences("notrash_config")` に同期する。フック側の [ConfigReader](app/src/main/java/com/notrash/xposed/ConfigReader.java) はフレームワークの設定を参照する。

- `camera_sound_unlock` / `essential_voice_unlock` の初期値はいずれも false。
- 未設定・取得失敗は OFF として扱う。
- 旧版の `/data/local/tmp/notrash_config.json` は新版では読み取らず、root 経由の JSON 出力もしない。
- UI の接続表示は Service の接続状態で更新する。自己フックで有効判定を作らない。
- `settingsSynced` は共有設定への書き込み結果。API 102 の `getRunningTargets()` では OS 側に読み込まれたバージョンと状態も確認する。

接続済みという表示は、すべての機能の成功を一括で保証するものではない。対象スコープ、アプリのバージョン、OS の実装が別途必要。

## スコープとライフサイクル

[scope.list](app/src/main/resources/META-INF/xposed/scope.list) はシステムフレームワークを `system` として指定する。Vector の実機ソースでも `app_pkg_name="system"` が取得条件。旧版の `android` 指定では OS 側にロードされなかった。

[NotrashModule](app/src/main/java/com/notrash/xposed/NotrashModule.java):

- `onModuleLoaded`: フレームワークの共有設定を取得。
- `onPackageReady`: そのプロセスの最初のパッケージだけを対象に、カメラ / Recorder / Space / SystemUI のフックを登録。
- `onSystemServerStarting`: `SystemServerHooks` を登録。
- `onHotReloading`: OS 側だけ更新を受け入れる。設定リスナーを解除し、OS の ClassLoader と生成済みの純正 Voice サービスを引き継ぐ。
- `onHotReloaded`: 古いフックを外して新しいフックと設定リスナーを登録。アプリ側は通常のプロセス再起動で更新する。

ホットリロードの初回ロードへの流用はしない。旧版が更新を拒否する場合や、そもそも対象としてロードされていない場合は再読み込みできない。Notrash の画面から Android 自体を自動再起動する処理はない。

## 機能ごとの変更

### カメラ

[CameraHooks](app/src/main/java/com/notrash/xposed/hooks/CameraHooks.java) は `Utils.initialize` の完了後、ON のときだけ音の強制フラグと設定のサポートを変更する。`pref_shutter_sound_key` の Preference の操作禁止も解除する。起動時フィールドの復元にはカメラを終了して開き直す。

### Recorder

[EssentialVoiceHooks](app/src/main/java/com/notrash/xposed/hooks/EssentialVoiceHooks.java) は `f5.i.P("NTF_ESSENTIAL_VOICE")` と対応するフレームワーク判定を ON 時だけ変更する。設定リスナーで Intro / Settings / Tutorial の3つの Activity を更新する。OFF では純正の機種判定へ戻す。

`NTF_ASTEROIDS` / `NTF_ASTEROIDS_PLUS` や Space の ASR エンジン選択を偽装する処理は削除済み。

### OS の Voice 入力

[SystemServerHooks](app/src/main/java/com/notrash/xposed/hooks/SystemServerHooks.java) は `NtExtServiceFactory.getOrCreate` の `NT_ESSENTIAL_VOICE` だけを対象にする。ON かつ純正が非対応用の実装の場合、既存の `NtServiceInjector` から純正 `NtEssentialVoiceImpl` を遅延生成して返す。

OFF では元の戻り値に戻し、生成済みの場合は音声入力停止と IME 状態更新を純正メソッドに依頼する。元から対応している機種では本来のサービスを使い続ける。生成中の再入を防ぎ、同じサービスを繰り返し作らない。

`InputMethodManagerService.getInputMethodNavButtonFlagsLocked` が `shouldShowVoiceInputIcon()` の結果に応じてビット `128` を付加する。そのため Recorder の画面だけの開放や SystemUI だけの再起動では不足していた。

ロック画面、パスワード欄、入力先、通信、認識結果の処理は純正のサービスを使用する。

## アプリ UI

- 枠線のないカード、NType のホームタイトル、ドットのセクション見出し、通常の本文書体、白黒のスイッチを使用。基本色は黒・白・赤。
- 表示設定 `high_contrast` は初期 true。ON は文字のコントラストを上げ、赤をセクション見出しと主要操作のアクセントに使用。OFF は落ち着いたモノクロ配色を使用。壁紙由来の色は取り入れない。表示設定はアプリ側に保存し、フックを有効化する設定とは分離する。
- `theme_mode` は `system` / `light` / `dark` から選択し、初期値は `system`。トグルはテーマのモノクロ色で表示する。
- 日本語のドット書体は、Nothing OS の `/system/fonts/Ndot77JPExtended.ttf` が読める場合に使用。同ファイルは APK にコピーしない。ない場合は同梱 Ndot にフォールバックする。
- 「このアプリについて」はデバイス情報画面の左2枚＋右の縦長カードと情報グリッドを参考にする。バージョン、接続フレームワーク、Android、機種は実際の値を表示する。
- クレジット画面は設けない。

参照した画面: ユーザー提供の Nothing デバイス情報、[Nothing X](https://play.google.com/store/apps/details?id=com.nothing.smartcenter)、[Nothing Weather](https://play.google.com/store/apps/details?id=com.nothing.weather)、[Nothing Gallery](https://play.google.com/store/apps/details?id=com.nothing.gallery) 、[Essential Space](https://play.google.com/store/apps/details?id=com.nothing.ntessentialspace) の公式掲載画像、実機の `com.nothing.soundrecorder`。

## 検証

JDK上の動作検査は [check_system_voice.py](verification/check_system_voice.py)、表示検査は [check_appearance.py](verification/check_appearance.py)、ADBの動作検査は [smoke.py](verification/smoke.py)。端末・アプリのバージョンと実機結果は [検証結果](verification/RESULTS.md)と各解析ページに記録する。
