# 香味特性別分類

## 1. 対象
- `香味特性別分類` は表示専用の導出値
- 保存元は `aromaIntensity` と `tasteComplexity`
- DB / backup / domain model に独立した永続列は持たない

## 2. 表示方式
- 5 x 5 グリッドで現在位置を表示する
- X軸は `aromaIntensity`
- Y軸は `tasteComplexity`
- グリッドをタップした場合は `aromaIntensity` と `tasteComplexity` を同時に更新する
- `aromaIntensity` または `tasteComplexity` を直接変更した場合は、グリッド表示も同期する
- どのセルも未分類にしないため、中央値 `MEDIUM` は低側に含める

## 3. 4タイプ

| value | 表示名 |
|--------|--------|
| SOUSHU | 爽酒 |
| KUNSHU | 薫酒 |
| JUNSHU | 醇酒 |
| JUKUSHU | 熟酒 |

## 4. 導出ルール
- `aromaIntensity` の `VERY_WEAK / WEAK / MEDIUM` を低側、`STRONG / VERY_STRONG` を高側とする
- `tasteComplexity` の `SIMPLE / SLIGHTLY_SIMPLE / MEDIUM` を低側、`SLIGHTLY_COMPLEX / COMPLEX` を高側とする
- 低香り × 低複雑性 = `SOUSHU`
- 高香り × 低複雑性 = `KUNSHU`
- 低香り × 高複雑性 = `JUNSHU`
- 高香り × 高複雑性 = `JUKUSHU`
