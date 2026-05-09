# レビュー項目

## アプリモード

| modeId | 表示名 | 内容 |
|---|---|---|
| normal | 通常 | 通常のレビュー入力 |
| kikisake_shi | 利酒師 | 利酒師向けの項目構成 |

## 項目一覧

| カテゴリ | 項目 | itemId | 保存フィールド | 通常 | 利酒師 | 型/選択肢 |
|---|---|---|---|---|---|---|
| 情報 | 日付 | DATE | date | 〇 | 〇 | LocalDate |
| 情報 | 価格 [円] | PRICE | price | 〇 | 〇 | Int |
| 情報 | 容量 [ml] | VOLUME | volume | 〇 | 〇 | Int |
| 情報 | 温度 | TEMPERATURE | temperature | 〇 | 〇 | temperature master |
| 情報 | 店名 | BAR | bar | 〇 | 〇 | String |
| 情報 | 料理 | DISH | dish | 〇 | 〇 | String |
| 情報 | 料理との相性 | FOOD_COMPATIBILITY | foodCompatibility | 〇 | 〇 | BAD, SLIGHTLY_BAD, MEDIUM, SLIGHTLY_GOOD, GOOD |
| 見た目 | 健全度 | APPEARANCE_SOUNDNESS | appearanceSoundness | - | 〇 | SOUND, UNSOUND |
| 見た目 | 色合い | APPEARANCE_COLOR | appearanceColor | 〇 | 〇 | color master |
| 見た目 | 粘性 | APPEARANCE_VISCOSITY | appearanceViscosity | 〇 | 〇 | 1..5 |
| 香り | 健全度 | AROMA_SOUNDNESS | aromaSoundness | - | 〇 | SOUND, UNSOUND |
| 香り | 強さ | AROMA_INTENSITY | aromaIntensity | 〇 | 〇 | intensity master |
| 香り | 上立ち香：具体例 | AROMA_EXAMPLES | aromaExamples | 〇 | 〇 | aroma master |
| 香り | 上立ち香：主体となる香り | AROMA_MAIN_NOTE | aromaMainNote | - | 〇 | String |
| 香り | 複雑性 | AROMA_COMPLEXITY | aromaComplexity | 〇 | - | complexity master |
| 味 | 健全度 | TASTE_SOUNDNESS | tasteSoundness | - | 〇 | SOUND, UNSOUND |
| 味 | アタック | TASTE_ATTACK | tasteAttack | 〇 | 〇 | attack master |
| 味 | テクスチャ：丸さ | TASTE_TEXTURE_ROUNDNESS | tasteTextureRoundness | 〇 | - | texture master |
| 味 | テクスチャ：舌ざわり | TASTE_TEXTURE_SMOOTHNESS | tasteTextureSmoothness | 〇 | - | texture master |
| 味 | テクスチャ：自由記述 | TASTE_TEXTURE_NOTE | tasteTextureNote | - | 〇 | String |
| 味 | 具体的な味わい：甘味 | TASTE_SWEETNESS | tasteSweetness | 〇 | - | taste scale |
| 味 | 具体的な味わい：酸味 | TASTE_SOURNESS | tasteSourness | 〇 | - | taste scale |
| 味 | 具体的な味わい：旨味 | TASTE_UMAMI | tasteUmami | 〇 | - | taste scale |
| 味 | 具体的な味わい：苦味 | TASTE_BITTERNESS | tasteBitterness | 〇 | - | taste scale |
| 味 | 具体的な味わい：自由記述 | TASTE_DESCRIPTION | tasteDescription | - | 〇 | String |
| 味 | 甘辛度 | TASTE_SWEET_DRYNESS | tasteSweetDryness | 〇 | 〇 | SWEET, MEDIUM_SWEET, MEDIUM_DRY, DRY |
| 味 | 含み香：強さ | TASTE_IN_PALATE_AROMA_INTENSITY | tasteInPalateAromaIntensity | 〇 | 〇 | intensity master |
| 味 | 含み香：具体例 | TASTE_IN_PALATE_AROMA_EXAMPLES | tasteInPalateAroma | 〇 | 〇 | aroma master |
| 味 | 余韻：長さ | TASTE_AFTERTASTE_LENGTH | tasteAftertaste | 〇 | 〇 | taste scale aftertaste labels |
| 味 | 余韻：記述 | TASTE_AFTERTASTE_NOTE | tasteAftertasteNote | - | 〇 | String |
| 味 | 複雑性 | TASTE_COMPLEXITY | tasteComplexity | 〇 | 〇 | complexity master |
| 特記 | 個性 | OTHER_INDIVIDUALITY | otherIndividuality | 〇 | 〇 | String |
| 特記 | 留意点 | OTHER_CAUTIONS | otherCautions | - | 〇 | String |
| 特記 | 日本酒4タイプ | OTHER_SAKE_TYPES | otherSakeTypes | 〇 | 〇 | SOUSHU, KUNSHU, JUNSHU, JUKUSHU（単一選択） |
| 特記 | 自由コメント | OTHER_FREE_COMMENT | otherFreeComment | 〇 | 〇 | String |
| 特記 | 総合評価 | OTHER_OVERALL_REVIEW | otherOverallReview | 〇 | 〇 | overall review master |

## レビュー入力・詳細表示

- 見た目の粘度は `1..5` を「低い」「やや低い」「中程度」「やや高い」「高い」として表示する。
- レビュー入力とレビュー詳細で区分けする場合は、`titleSmall` の subheader と余白で緩く区分けし、下位グループに Card / Divider / 背景色ブロックを使わない。
- subheader グループに属する項目の配置完了後は、後続の通常項目との間を特に大きく空け、次グループとも大きい余白で区切る。直前が subheader グループの場合、次 subheader の上余白は置かない。
- 香りは「上立香」の見出しで区分けし、配下に「強さ」「具体例」を表示する。香りの複雑性は「複雑性」と表示する。
- 味は入力・詳細とも以下の順で表示し、レビュー詳細では味の健全度を表示しない。
  - アタック
  - テクスチャ: 丸さ、舌ざわり
  - 味わい: 甘味、酸味、苦味、旨味
  - 甘辛度
  - 含み香: 強さ、具体例
  - 余韻（自由記述がある場合は直後に表示）
  - 複雑性
- 特記の日本酒4タイプは、薫酒 / 熟酒 / 爽酒 / 醇酒の4択として2x2グリッドで単一選択する。保存フィールドは既存互換のため `otherSakeTypes` のリスト型を維持し、選択値を1件だけ保持する。
