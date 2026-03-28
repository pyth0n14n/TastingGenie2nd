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
  "schemaVersion": 3,
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

- 現行の JSON backup は画像バイト列を含めない
- `Sake.imageUri` はアプリ専用領域の URI を指すため、端末やインストール状態をまたいで復元可能な値ではない
- そのため、画像は export/import 対象にしない
- 画像付き backup の扱いは ZIP 化を行う後続 PR で定義する
