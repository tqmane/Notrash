# Nothing Camera 16.0.01.26.00 の解析

対象パッケージは `com.nothing.camera`。Notrash は純正カメラのシャッター音設定を表示し、マナーモード中も切り替えられるようにする。カメラ APK 自体は書き換えない。

## 解析対象

| 項目 | 値 |
| --- | --- |
| APK | `apk/com.nothing.camera_16.0.01.26.00.apk` |
| versionCode | `160012600` |
| 最小 / 対象 Android API | `34 / 36` |
| 実機 | A059 / Asteroids、Android 16 |
| SHA-256 | `25001c9efbce90c99e987c28a31cf89a716b53190aef71a3d7f5107e5c8e03ce` |

解析日・実機確認日: 2026-09-06。以下のクラス名と実装はこのバージョンについての情報。

## シャッター音を決める流れ

1. 起動時に `com.nothing.common.setting.Utils.initialize(Context,int,int)` を実行する。
2. `Utils.isCameraSoundForced(context)` の結果を `ProductConfig.isCameraSoundForced` に保存する。
3. `ProductConfig.isSupportShutterSound &= !isCameraSoundForced` により、音が強制される環境では設定項目も無効になる。
4. `SettingGroupsManager` がカメラの設定一覧を組み立てる。
5. 撮影時は `SettingContext.isShutterSoundEnabled()` が強制フラグ、マナーモード、保存済み設定、モーション写真の状態などを確認する。

強制判定には、フレームワークの `config_camera_sound_forced`、`audio.camerasound.force`、日本向け設定、SIM / ネットワークの国コードなどが使われる。新旧の判定経路は `Utils.isCameraSoundForcedNew/Old` に分かれる。

## 表示されても操作できない理由

設定項目の表示と、スイッチの操作可否は別の処理。

`SettingGroupsManager.filterShutterSoundPreference(Context)` は、マナーモード中かつシャッター音の強制フラグがない場合に、対象の `ListPreference` へ次の設定を行う。

```java
listPreference.setOverrideValue("off");
listPreference.setClickable(false);
```

`SettingApplier.applyCaptureRequestBuilderToUI` にもマナーモードに応じた設定処理がある。このため、起動時の強制フラグだけを変えても設定がグレーアウトする場合がある。

## Notrash が変更する箇所

実装: [CameraHooks.java](../app/src/main/java/com/notrash/xposed/hooks/CameraHooks.java)

| 箇所 | Notrash の設定が ON のとき |
| --- | --- |
| `Utils.initialize` の完了後 | 強制音フラグを解除し、シャッター音設定のサポートを有効にする |
| 音の強制判定 | 強制されていない結果を返す |
| `Util.isInSilentMode` | カメラ内の判定を false にする |
| `ListPreference` | キーが `pref_shutter_sound_key` の項目だけ、強制 OFF・操作禁止を解除する |
| 設定画面の再構築 | 設定が再度ロックされる経路でも ON/OFF を参照する |

設定 OFF では元の処理を実行する。起動時に保存されるフィールドを元へ戻すには、カメラを終了して開き直す。初期状態でフィールドを書き換えていた旧実装は修正済み。

純正のシャッター音スイッチと、Notrash の「制限解除」スイッチは別。Notrash を ON にするだけでシャッター音そのものを常に消す仕様ではない。

## 実機で確認したこと

- Notrash OFF → カメラを開き直すと、制限対象のシャッター音設定が非表示。
- Notrash ON → カメラを開き直すと、マナーモード中でもシャッター音設定が表示され、操作可能。
- 撮影・録画はこの検証では実行していない。実際の音量や、全撮影モードでの音再生は未評価。

再実行: `python verification/smoke.py`。画面記録: [camera-on.png](../verification/camera-on.png)。

## 解析元

ワークスペース直下の `decompiled_classes5/sources/com/nothing/common/`:

- `setting/Utils.java`: 起動時の設定と強制音判定。
- `utils/ProductConfig.java`: 起動後に参照される設定フィールド。
- `setting/SettingGroupsManager.java`: 設定表示とマナーモード連動。
- `setting/SettingApplier.java`: 撮影状態から UI への設定反映。
- `setting/SettingContext.java`: 撮影時の音の有効判定。

[全体の導入手順](../README.md) · [Essential Recorder](essential-recorder.md) · [Essential Space](essential-space.md)
