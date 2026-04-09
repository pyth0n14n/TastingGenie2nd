# PLAN.md 統合版: テイスティングノート再構成と残TODO

## Summary
- 進め方は `spec-first`。各実装 PR は `docs/spec/data_model.md`、関連 master spec、`docs/spec/qa.md` を同時更新する。
- Review の DB は分割しない。`reviews` 1 テーブルを維持し、外観/香り/味わい/その他に属する列だけを `appearanceXxx / aromaXxx / tasteXxx / otherXxx` で明示する。
- `香味特性別分類（薫酒/爽酒/熟酒/醇酒）` は保存しない。`aromaIntensity × tasteComplexity` から UI で導出する。

## Canonical Data Model
- Review の共通項目は現状維持: `date`, `bar`, `price`, `volume`, `temperature`, `scene`, `dish`
- 外観: `appearanceSoundness`, `appearanceColor`, `appearanceViscosity`
- 香り: `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`, `aromaComplexity`
- 味わい: `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`, `tasteTextureSmoothness`, `tasteMainNote`, `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`, `tasteInPalateAroma`, `tasteAftertaste`, `tasteComplexity`
- その他: `otherIndividuality`, `otherCautions`, `otherOverallReview`
- 現行 Review 項目の移行先:
  `color -> appearanceColor`, `viscosity -> appearanceViscosity`, `intensity -> aromaIntensity`, `scentTop -> aromaExamples`, `scentMouth -> tasteInPalateAroma`, `sweet -> tasteSweetness`, `sour -> tasteSourness`, `bitter -> tasteBitterness`, `umami -> tasteUmami`, `sharp -> tasteAftertaste`, `review -> otherOverallReview`, `comment -> otherCautions`
- 廃止項目: `scentBase`
- 追加設定: `AppSettings.showReviewSoundness`
- 追加酒項目: `Sake.isPinned`
- 追加分類: `SakeClassification.FUTSUSHU`

## Master / UI Decisions
- 健全度は 2 値: `健全 / 不健全`
- 香り複雑性・味わい複雑性は共通 5 段階: `シンプル / ややシンプル / 中程度 / やや複雑 / 複雑`
- アタックは 5 段階: `弱い / やや弱い / 中程度 / やや強い / 強い`
- テクスチャーは 2 軸:
  `tasteTextureRoundness` は 6 段階、`tasteTextureSmoothness` は 5 段階
- `aromaExamples` と `tasteInPalateAroma` は既存 `Aroma` マスタを使う
- 温度は既存 `temperature` マスタ順の scale bar に変更する
- 量 shortcut は `グラス 120mL / 1合 180mL / 四合瓶 720mL / 一升瓶 1800mL` を `volume:Int` に代入し、手入力も残す
- 健全度を非表示にした場合は 3項目とも `健全` を既定値として扱う
- `ReviewEdit` と `ReviewDetail` は両方とも `外観 / 香り / 味わい / その他` タブにする
- aroma wheel は後続 PR で導入し、それまでは既存 aroma データを使う

## PR Plan
1. `docs(review-v2)`: この統合方針で `data_model`, master spec, `qa.md` を更新する。新規 master は `soundness`, `complexity`, `attack`, `texture` を定義する。
2. `fix(review)`: レビュー編集保存後に Detail が即時反映されるよう修正する。中央タップで staged control が反応しない bug もここで直す。
3. `feat(validation)`: 必須項目マーク、フィールド単位の具体的エラー表示、数値境界値と異常入力の no-crash を sake/review で統一する。
4. `feat(review-schema-v4)`: Room `3 -> 4`、backup schema `3 -> 4`、`Review/ReviewInput/ReviewEntity/SerializableReview` を canonical 名へ移行する。
5. `feat(review-shell)`: edit/detail のタブ骨格を作り、温度 scale と volume shortcut を共通 UI として入れる。
6. `feat(review-appearance-aroma)`: `appearanceXxx` と `aromaXxx` を実装する。
7. `feat(review-taste-other)`: `tasteXxx` と `otherXxx`、4タイプ分類グリッド、双方向同期を実装する。
8. `feat(review-aroma-wheel)`: `aromaExamples` UI を aroma wheel に差し替える。画像/最終仕様到着後に着手する。
9. `feat(sake-type)`: `FUTSUSHU` (普通酒) を追加し、分類 `OTHER` 自由記述欄の位置ずれを修正する。
10. `feat(sake-list-v5)`: `Sake.isPinned` を追加し、Room `4 -> 5`、backup schema `4 -> 5`、一覧上部固定と最新総合評価表示を入れる。
11. `feat(image)`: gallery に加えて camera capture を追加する。

## Test Plan
- migration: 旧 review データが canonical 列へ正しく移り、`scentBase` が消え、`comment` が `otherCautions` へ移ること
  ただし、リリース前なので後方互換は不要で、DBの破壊的変更を許可する。リリース前に version 1に戻す予定。
- round-trip: 新 review 項目と `isPinned` が save/load/export/import で崩れないこと
- review UI: edit/detail 両方でタブ切替、保存後再表示、温度 scale、volume shortcut、texture 中央タップ、4タイプ分類の双方向同期が動くこと
- validation: 必須未入力、精米歩合境界値、異常文字列、`NaN/Infinity`、不正 enum でもクラッシュしないこと
- sake UI: `FUTSUSHU` 表示、`OTHER` 自由記述欄位置、pin 並び順、最新総合評価表示
- image: gallery/camera の両方で save/cancel/replace/delete が既存画像 cleanup ルールを壊さないこと

## Assumptions
- 共通項目 `date/bar/price/volume/temperature/scene/dish` はそのまま保持し、prefix 化しない
- `showReviewSoundness` の初期値は `true`
- `香味特性別分類` は DB 保存しない
- `tasteAftertaste` は既存 5 段階 taste scale を流用する
- `甘辛度` の独立列は作らず、味わい評価は `tasteSweetness / tasteSourness / tasteBitterness / tasteUmami / tasteAftertaste` で扱う

