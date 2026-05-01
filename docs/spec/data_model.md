# データモデル

## 1. 設計方針

- static（酒情報）と dynamic（レビュー）を分離する
- 1銘柄に複数レビューを紐付け可能とする
- Review は 1 レコード = 1 件のテイスティングノートとして保持する
- Review 内の評価項目は `common` / `appearance` / `aroma` / `taste` / `other` の論理グループで扱う
- Review の永続化は 1 テーブルのままとし、列名は `appearanceXxx / aromaXxx / tasteXxx / otherXxx` で所属を明示する
- 画像は URI ベースで `Sake.imageUris` に保持する
- 酒削除は SakeList から確認ダイアログ経由で実行し、紐づくレビューを同時に削除する
- 酒画像は保存時に app-managed URI へ取り込み、削除・差し替え時は既定で参照解除のみ行う
- レビュー削除は ReviewList から確認ダイアログ経由で実行する
- マスタデータは `docs/spec/master/*.md` を source of truth とし、実装では `assets/master/*.json` に反映する
- `香味特性別分類` は `aromaIntensity × tasteComplexity` から導出する表示値であり、DB には保存しない

---

## 2. Sake（static）

| 項目 | 型 | 必須 |
|------|----|------|
| id | Long | PK |
| name | String | 必須 |
| grade | Enum | 必須 |
| imageUris | List<String(URI)> | 任意 |
| gradeOther | String | 任意 |
| type | List<Enum> | 任意 |
| typeOther | String | 任意 |
| maker | String | 任意 |
| prefecture | Enum | 任意 |
| city | String | 任意 |
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

### 3.1 Common

| 項目 | 型 | 必須 |
|------|----|------|
| id | Long | PK |
| sakeId | Long | FK |
| date | LocalDate | 必須 |
| bar | String | 任意 |
| price | Int (1..1,000,000) | 任意 |
| volume | Int(mL, 1..25,000) | 任意 |
| temperature | Enum | 任意 |
| scene | String（料理との相性） | 任意 |
| dish | String | 任意 |

### 3.2 Appearance

| 項目 | 型 | 必須 |
|------|----|------|
| appearanceSoundness | Enum | 必須（既定値: SOUND） |
| appearanceColor | Enum | 任意 |
| appearanceViscosity | Int(1-5) | 任意 |

### 3.3 Aroma

| 項目 | 型 | 必須 |
|------|----|------|
| aromaSoundness | Enum | 必須（既定値: SOUND） |
| aromaIntensity | Enum | 任意 |
| aromaExamples | List<Enum> | 任意 |
| aromaMainNote | String | 任意 |
| aromaComplexity | Enum | 任意 |

### 3.4 Taste

| 項目 | 型 | 必須 |
|------|----|------|
| tasteSoundness | Enum | 必須（既定値: SOUND） |
| tasteAttack | Enum | 任意 |
| tasteTextureRoundness | Enum | 任意 |
| tasteTextureSmoothness | Enum | 任意 |
| tasteMainNote | String | 任意 |
| tasteSweetness | Enum | 任意 |
| tasteSourness | Enum | 任意 |
| tasteBitterness | Enum | 任意 |
| tasteUmami | Enum | 任意 |
| tasteInPalateAroma | List<Enum> | 任意 |
| tasteAftertaste | Enum | 任意 |
| tasteComplexity | Enum | 任意 |

### 3.5 Other

| 項目 | 型 | 必須 |
|------|----|------|
| otherIndividuality | String | 任意 |
| otherCautions | String | 任意 |
| otherFreeComment | String | 任意 |
| otherOverallReview | Enum | 任意 |

---

## 4. AppSettings

| 項目 | 型 | 必須 |
|------|----|------|
| showHelpHints | Boolean | 必須（既定値: true） |
| showReviewSoundness | Boolean | 必須（既定値: true） |
| autoDeleteUnusedImages | Boolean | 必須（既定値: false） |

- `showReviewSoundness = false` のとき、UI は健全度入力を表示しない
- 非表示時でも Review の 3 つの健全度は `SOUND` を既定値として扱う
- `autoDeleteUnusedImages = true` のとき、酒削除時と画像差し替え保存時に未参照 app-managed 画像 cleanup を自動実行する

---

## 5. 関係

Sake (1) ---- (N) Review

---

## 6. Enum / Scale 定義

### 6.1 Sake系

- `grade`: `docs/spec/master/sake_type.md`
- `type`: `docs/spec/master/classification.md`
- `prefecture`: `docs/spec/master/prefecture.md`

### 6.2 Review系（保存対象）

- `temperature`: `docs/spec/master/temperature.md`
- `appearanceSoundness`, `aromaSoundness`, `tasteSoundness`: `docs/spec/master/soundness.md`
- `appearanceColor`: `docs/spec/master/color.md`
- `appearanceViscosity`: `1..5` の固定スケールを使う（1=とても弱い、3=中程度、5=とても強い）
- `aromaIntensity`: `docs/spec/master/intensity.md`
- `aromaExamples`, `tasteInPalateAroma`: `docs/spec/master/aroma.md`
- `aromaComplexity`, `tasteComplexity`: `docs/spec/master/complexity.md`
- `tasteAttack`: `docs/spec/master/attack.md`
- `tasteTextureRoundness`, `tasteTextureSmoothness`: `docs/spec/master/texture.md`
- `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`, `tasteAftertaste`: `docs/spec/master/taste_scale.md`
- `otherOverallReview`: `docs/spec/master/overall_review.md`

### 6.3 Review系（導出表示）

- `香味特性別分類`: `docs/spec/master/flavor_profile.md`

---

## 7. 保持形式

- `LocalDate` は日付として保存する
- `List<Enum>` は JSON 文字列で永続化する
- `imageUris: List<String>` も JSON 文字列で永続化する
- Review の評価項目は 1 テーブル `reviews` に prefix 付き列で保持する
- 画像は `Sake.imageUris` に 0..N 件保持する
- 一覧カードなど単一 preview が必要な画面では `imageUris.firstOrNull()` を代表画像として使う
- `Review` は画像を直接保持しない
- `香味特性別分類` は保存せず、画面表示時に導出する

---

## 8. 外部入力とバリデーション

- `assets/master/*.json`、import JSON、URI などの外部入力は信頼しない
- Enum を表す `value` は spec で定義された値と一致することを実行時に検証する
- 不正な `value` を受け取った場合はクラッシュさせず、UI に失敗状態として表示する
- `showReviewSoundness = false` のときも、健全度の保存値は `SOUND` を前提とし、`null` や未定義値を保存しない
