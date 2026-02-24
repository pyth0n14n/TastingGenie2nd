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

- `sakes`: `id` をキーに upsert
- `reviews`: `id` をキーに upsert
- `reviews.sakeId` が存在しない場合は失敗扱い
