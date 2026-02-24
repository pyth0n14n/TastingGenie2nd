# データモデル

## 1. 設計方針

- static（酒情報）と dynamic（レビュー）を分離
- 1銘柄に複数レビューを紐付け可能
- 画像はURIベース管理
- 第1マイルストーンでは削除機能を実装しない
- マスタデータは `assets/master/*.json` を単一ソースとする

---

## 2. Sake（static）

| 項目 | 型 | 必須 |
|------|----|------|
| id | Long | PK |
| name | String | 必須 |
| grade | Enum | 必須 |
| type | List<Enum> | 任意 |
| typeOther | String | 任意 |
| maker | String | 任意 |
| prefecture | Enum | 任意 |
| alcohol | Int | 任意 |
| kojiMai | String | 任意 |
| kojiPolish | Int | 任意 |
| kakeMai | String | 任意 |
| kakePolish | Int | 任意 |
| sakeDegree | Float | 任意 |
| acidity | Float | 任意 |
| amino | Float | 任意 |
| yeast | String | 任意 |
| water | String | 任意 |

---

## 3. Review（dynamic）

| 項目 | 型 | 必須 |
|------|----|------|
| id | Long | PK |
| sakeId | Long | FK |
| date | LocalDate | 必須 |
| bar | String | 任意 |
| price | Int | 任意 |
| volume | Int | 任意 |
| temperature | Enum | 任意 |
| color | Enum | 任意 |
| viscosity | Int(1-3) | 任意 |
| intensity | Enum | 任意 |
| scentTop | List<Enum> | 任意 |
| scentBase | List<Enum> | 任意 |
| scentMouth | List<Enum> | 任意 |
| sweet | Enum | 任意 |
| sour | Enum | 任意 |
| bitter | Enum | 任意 |
| umami | Enum | 任意 |
| sharp | Enum | 任意 |
| scene | String | 任意 |
| dish | String | 任意 |
| comment | String | 任意 |
| review | Enum | 任意 |
| imageUri | String(URI) | 任意 |

---

## 4. 関係

Sake (1) ---- (N) Review

---

## 5. Enum定義

### 5.1 Sake系

- `grade`: `docs/spec/master/sake_type.md` に定義
- `type`: `docs/spec/master/classification.md` に定義
- `prefecture`: `docs/spec/master/prefecture.md` に定義

### 5.2 Review系

- `temperature`: `docs/spec/master/temperature.md` に定義
- `color`: `docs/spec/master/color.md` に定義
- `intensity`: `docs/spec/master/intensity.md` に定義
- `scentTop`, `scentBase`, `scentMouth`: `docs/spec/master/aroma.md` に定義
- `sweet`, `sour`, `bitter`, `umami`, `sharp`: `docs/spec/master/taste_scale.md` に定義
- `review`: `docs/spec/master/overall_review.md` に定義

---

## 6. 保持形式

- `LocalDate` は日付として保存する
- `List<Enum>` は JSON 文字列で永続化する
- 画像は `Review.imageUri` に 1件保持する
