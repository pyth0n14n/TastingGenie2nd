# データモデル

## 1. 設計方針

- static（酒情報）と dynamic（レビュー）を分離する
- 1銘柄に複数レビューを紐付け可能とする
- Review は 1 レコード = 1 件のテイスティングノートとして保持する
- Review 内の評価項目は `common` / `appearance` / `aroma` / `taste` / `other` の論理グループで扱う
- Review の永続化は 1 テーブル `reviews` のままとし、列名は `appearanceXxx / aromaXxx / tasteXxx / otherXxx` で所属を明示する
- レビュー項目の表示有無は `review_modes` / `review_mode_items` で管理し、現在選択中のモードIDは `AppSettings.reviewModeId` に保持する
- 標準モードは `normal`（通常）と `kikisake_shi`（利酒師）を seed する
- 将来のカスタムモードは `review_modes` / `review_mode_items` に追加する。Review 自体には作成時モードを保存しない
- 画像は URI ベースで `Sake.imageUris` に保持する
- マスタデータは `docs/spec/master/*.md` を source of truth とし、実装では `assets/master/*.json` に反映する
- ヘルプメッセージは `docs/spec/review_help_messages.md` に保存し、UI連携は後続対応とする

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

詳細な項目・表示モード・ヘルプ文言は以下を参照する。

- `docs/spec/review_items.md`
- `docs/spec/review_help_messages.md`

### 3.1 Common

| 項目 | 型 | 必須 |
|------|----|------|
| id | Long | PK |
| sakeId | Long | FK |
| date | LocalDate | 必須 |
| price | Int (1..1,000,000) | 任意 |
| volume | Int(mL, 1..25,000) | 任意 |
| temperature | Enum | 任意 |
| bar | String | 任意 |
| dish | String | 任意 |
| foodCompatibility | Enum | 任意 |

### 3.2 Appearance

| 項目 | 型 | 必須 |
|------|----|------|
| appearanceSoundness | Enum | 必須（既定値: SOUND） |
| appearanceColor | Enum | 任意 |
| appearanceColorOther | String | 任意。appearanceColor が OTHER の場合に使用 |
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
| tasteTextureNote | String | 任意 |
| tasteSweetness | Enum | 任意 |
| tasteSourness | Enum | 任意 |
| tasteBitterness | Enum | 任意 |
| tasteUmami | Enum | 任意 |
| tasteDescription | String | 任意 |
| tasteSweetDryness | Enum | 任意 |
| tasteInPalateAromaIntensity | Enum | 任意 |
| tasteInPalateAroma | List<Enum> | 任意 |
| tasteAftertaste | Enum | 任意 |
| tasteAftertasteNote | String | 任意 |
| tasteComplexity | Enum | 任意 |

### 3.5 Other

| 項目 | 型 | 必須 |
|------|----|------|
| otherIndividuality | String | 任意 |
| otherCautions | String | 任意 |
| otherSakeTypes | List<Enum> | 任意 |
| otherFreeComment | String | 任意 |
| otherOverallReview | Enum | 任意 |

---

## 4. ReviewMode

| テーブル | 項目 | 型 | 内容 |
|------|------|----|------|
| review_modes | id | String | `normal` / `kikisake_shi` / 将来のカスタムID |
| review_modes | label | String | 表示名 |
| review_modes | isBuiltIn | Boolean | 標準モードかどうか |
| review_mode_items | modeId | String | review_modes.id |
| review_mode_items | itemId | String | ReviewItemId |
| review_mode_items | isEnabled | Boolean | モード内で表示・保存対象にするか |

- 設定画面のモード切替は `AppSettings.reviewModeId` を更新する
- ReviewEdit は選択中モードの有効項目のみ表示する
- 非表示項目は保存時に未入力として扱う。ただし健全度は `SOUND` を保持する

---

## 5. AppSettings

| 項目 | 型 | 必須 |
|------|----|------|
| showHelpHints | Boolean | 必須（既定値: true） |
| showReviewSoundness | Boolean | 必須（既定値: true） |
| autoDeleteUnusedImages | Boolean | 必須（既定値: false） |
| reviewModeId | String | 必須（既定値: `normal`） |

---

## 6. Enum / Scale 定義

- `temperature`: `docs/spec/master/temperature.md`
- `appearanceSoundness`, `aromaSoundness`, `tasteSoundness`: `docs/spec/master/soundness.md`
- `appearanceColor`: `docs/spec/master/color.md`
- `appearanceViscosity`: `1..5`
- `aromaIntensity`, `tasteInPalateAromaIntensity`: `docs/spec/master/intensity.md`
- `aromaExamples`, `tasteInPalateAroma`: `docs/spec/master/aroma.md`
- `aromaComplexity`, `tasteComplexity`: `docs/spec/master/complexity.md`
- `tasteAttack`: `docs/spec/master/attack.md`
- `tasteTextureRoundness`, `tasteTextureSmoothness`: `docs/spec/master/texture.md`
- `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`, `tasteAftertaste`: `docs/spec/master/taste_scale.md`
- `foodCompatibility`: `docs/spec/review_items.md`
- `tasteSweetDryness`: `docs/spec/review_items.md`
- `otherSakeTypes`: `docs/spec/master/flavor_profile.md`
- `otherOverallReview`: `docs/spec/master/overall_review.md`

---

## 7. 保持形式

- `LocalDate` は日付として保存する
- `List<Enum>` は JSON 文字列で永続化する
- `imageUris: List<String>` も JSON 文字列で永続化する
- `Review` は画像を直接保持しない
- 旧 `scene` / `tasteMainNote` は新DB構成では保持しない
- DB v9 から v10 への migration では基本情報だけ保持し、旧レビュー評価項目は初期化する
