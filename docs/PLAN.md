# ききさけ帖プラン

## バックアップ/リストア刷新

### 目的

- 既存の JSON 差分インポート/エクスポートを廃止し、ZIP 形式のフルバックアップへ置き換える。
- バックアップ対象は DB データ、設定データ、app-managed 酒画像一式とする。
- リストアはマージせず、バックアップ内容で現在の DB・設定・画像参照を上書きする。
- 旧 JSON バックアップは非対応とし、読み込み時はエラー表示する。

### 実装方針

- バックアップ拡張子は `.zip`、MIME は `application/zip`。
- ZIP 内は `manifest.json`、`data.json`、`images/sakes/*`。
- `data.json` は `schemaVersion = 10` とし、`sakes`、`reviews`、`reviewModes`、`reviewModeItems`、`settings` を含める。
- `sakes.imageUris` は ZIP 内の相対画像パスとして保存し、復元時に新しい app-managed URI へ置換する。
- `ImportExportRepository` は `exportBackup(OutputStream)` / `restoreBackup(InputStream)` のストリーム API とする。
- 復元前に ZIP 構造、schema version、DB参照、画像参照を検証し、失敗時は既存 DB を削除しない。
- 復元成功後、DB から参照されなくなった app-managed 酒画像を削除する。

### テスト

- ZIP export が DB、設定、画像を含むこと。
- ZIP restore が既存 DB 行を残さず、バックアップ内容だけに置き換えること。
- 画像付き酒を export/restore したとき、復元後の `imageUris` が新しい app-managed URI になり、画像ファイルが読めること。
- 壊れた ZIP、必須ファイル欠落、未対応 version、存在しない画像参照、レビューの不正 `sakeId` は失敗すること。
- restore 失敗時に既存 DB が削除されないこと。
- 標準確認は `./gradlew localFix`。
