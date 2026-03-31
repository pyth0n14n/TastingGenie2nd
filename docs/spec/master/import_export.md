# インポート/エクスポート仕様

## 1. 実装時期

- 第2マイルストーン（M2）で実装する
- 形式は ZIP とする

---

## 2. 形式

- 拡張子: `.zip`
- ZIP 直下に `backup.json` を持つ
- 画像がある場合は `images/sakes/...` に格納する
- `backup.json` は UTF-8 JSON とする

```json
{
  "schemaVersion": 4,
  "sakes": [],
  "reviews": []
}
```

---

## 3. バリデーション

- `schemaVersion` 必須
- 未対応バージョンは読み込み失敗としてUIに表示
- `backup.json` 欠損時は読み込み失敗としてUIに表示
- ZIP破損時は読み込み失敗としてUIに表示
- `backup.json` 破損時は読み込み失敗としてUIに表示

---

## 4. 読み込みポリシー

- バックアップ内の `id` / `sakeId` は payload 内参照用であり、端末ローカルの DB 主キーとしては再利用しない
- `sakes`: 同値行が既存 DB にない場合だけ新しいローカル ID を採番して追加する
- `reviews`: 同値行が既存 DB にない場合だけ新しいローカル ID を採番して追加する
- `reviews.sakeId` は同じ payload 内の `sakes.id` を参照している必要がある
- payload 内で `sakes.id` が重複している場合は失敗扱い
- `sakes` の同値判定は、画像そのものではなく「画像あり/なし」を含む全項目一致で行う
- `reviews` の同値判定は、解決後のローカル `sakeId` とレビュー全項目一致で行う
- 同値判定で既存 row を再利用できる場合、画像 import は実行しない

---

## 5. 画像の扱い

- `Sake.imageUri` は backup manifest に直接書かない
- 画像がある酒は `SerializableSake.imagePath` に ZIP 内相対パスを書き込む
- 実画像は `images/sakes/...` にバイト列として格納する
- import 時に `imagePath` があるのに ZIP 内画像が欠けている場合は失敗扱い
- review は画像を持たない
