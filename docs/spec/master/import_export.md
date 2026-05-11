# バックアップ/リストア仕様

## 1. 形式

- バックアップ形式は ZIP のみ
- 拡張子: `.zip`
- MIME: `application/zip`
- 旧 JSON バックアップは読み込み対象外とし、読み込み時は失敗として UI に表示する

---

## 2. ZIP 構成

```text
manifest.json
data.json
images/sakes/*
```

- `manifest.json` はバックアップ形式と `schemaVersion` を保持する
- `data.json` は UTF-8 JSON とする
- `images/sakes/*` は `Sake.imageUris` から参照される app-managed 酒画像を格納する

---

## 3. data.json

```json
{
  "schemaVersion": 10,
  "sakes": [],
  "reviews": [],
  "reviewModes": [],
  "reviewModeItems": [],
  "settings": {
    "showHelpHints": true,
    "showReviewSoundness": true,
    "autoDeleteUnusedImages": false,
    "reviewModeId": "normal"
  }
}
```

- `schemaVersion` 必須
- schemaVersion 10 は ZIP フルバックアップ形式を表す
- 未対応バージョンは読み込み失敗として UI に表示する
- ZIP破損、必須ファイル欠落、JSON破損は読み込み失敗として UI に表示する

---

## 4. 読み込みポリシー

- リストアは既存データへマージしない
- バックアップ検証後、現在の DB データ、設定、画像参照をバックアップ内容で上書きする
- `sakes` / `reviews` の `id` はバックアップ内の値をそのまま復元する
- `reviews.sakeId` は同じ payload 内の `sakes.id` を参照している必要がある
- `reviewModeItems.modeId` は同じ payload 内の `reviewModes.id` を参照している必要がある
- payload 内で主キーが重複している場合は失敗扱い
- 復元失敗時は既存 DB を削除しない

---

## 5. 画像の扱い

- export は app-managed 酒画像を `images/sakes/*` に同梱する
- `data.json` 内の `Sake.imageUris` は端末ローカル URI ではなく ZIP 内の相対画像パスとする
- restore は相対画像パスを検証し、画像を app-managed 領域へ新しいファイル名でコピーする
- restore 後の `Sake.imageUris` は新しい app-managed file URI に置換する
- `data.json` が参照する画像が ZIP 内に存在しない場合は失敗扱い
- 復元成功後、DB から参照されなくなった app-managed 酒画像は削除する
