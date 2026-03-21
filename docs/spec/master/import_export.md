# インポート/エクスポート仕様

## 1. 実装時期

- 第2マイルストーン（M2）で実装する
- 形式は JSON のみ

---

## 2. 形式

- UTF-8
- 拡張子: `.json`
- 1ファイルに `sakes` と `reviews` を含める

```json
{
  "schemaVersion": 1,
  "sakes": [],
  "reviews": []
}
```

---

## 3. バリデーション

- `schemaVersion` 必須
- 未対応バージョンは読み込み失敗としてUIに表示
- JSON破損時は読み込み失敗としてUIに表示

---

## 4. 読み込みポリシー

- バックアップ内の `id` / `sakeId` は payload 内参照用であり、端末ローカルの DB 主キーとしては再利用しない
- `sakes`: import 時に新しいローカル ID を採番して追加する
- `reviews`: import 時に新しいローカル ID を採番して追加する
- `reviews.sakeId` は同じ payload 内の `sakes.id` を参照している必要がある
- payload 内で `sakes.id` が重複している場合は失敗扱い

---

## 5. 画像の扱い

- review 画像は URI ベース管理のため、バックアップには画像バイト列を含めない
- `imageUri` は端末や権限状態をまたいで復元可能な値ではないため、export/import 対象にしない
