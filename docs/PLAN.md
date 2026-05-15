# 酒リスト・酒編集・レビューUI修正計画

## Summary

酒リスト、酒編集、レビューリスト、レビュー編集の指摘をまとめて反映する。仕様優先のため、`docs/spec/` も同じ変更単位で更新する。特にアルコール小数対応は `Int` から `Float` 相当への永続化変更になるため DB migration と schema 更新を含める。

## Key Changes

- 酒リスト
  - サムネイルを現状の `56dp` から大きくし、銘柄名をより目立つ typography に上げる。
  - 評価表示を「最新評価」から「平均評価」に変更する。
  - `SakeListSummary` / Room query / 表示ラベルを平均値ベースに変更し、レビューリストの平均評価と同じく未評価レビューを除外して `%.2f` 表示にする。
  - contentDescription と文字列リソースも「最新評価」から「平均評価」に更新する。

- 酒編集
  - カメラ撮影画像の `file://.../capture-*.jpg` で `ContentResolver.getType()` が `null` になるケースを許可する。
  - MIME が取れない場合は URI/ファイル拡張子から `jpg/jpeg/png/webp` を判定し、管理ディレクトリへコピーする。
  - アルコールを小数入力可能にし、UI keyboard を decimal に変更する。
  - `Sake.alcohol` / `SakeInput.alcohol` / `SakeEntity.alcohol` を `Float?` に変更し、DB v11 migration で `sakes.alcohol` を `REAL` に作り直して既存整数値を保持する。
  - `docs/spec/data_model.md` の `alcohol` 型を `Float` に更新する。

- レビューリスト
  - 香り chip は「上立ち香から最大2件」「含み香から最大2件」を維持しつつ、横一列固定で潰れないよう `FlowRow` へ変更する。
  - chip テキストは 1 行表示を保ち、必要なら次行に回す。
  - タイムライン本文は `otherFreeComment` ではなく `otherIndividuality` を表示する。

- レビュー編集
  - AppBar タイトルは `銘柄: {name}` ではなく `{name}` のみ表示する。
  - 情報欄の店名・料理など通常テキストフィールドに `Modifier.fillMaxWidth()` が渡るようにし、右端まで枠を広げる。
  - `* は必須項目です` は `ReviewSection.BASIC` のページだけに表示し、香り・味わい・外観・特記タブでは表示しない。

## Tests

- Unit tests
  - `SakeListViewModelTest` / repository fake を平均評価対応へ更新し、未評価レビュー除外と `4.50` 表示相当を確認する。
  - `SakeEditViewModelTest` に `16.5` が保存されるケース、`16%` が invalid になるケースを追加・更新する。
  - `SakeImageRepositoryImplTest` に MIME `null` の `file://...jpg` が import できるケースを追加する。
  - migration test に v10 -> v11 で `alcohol REAL` かつ既存値保持を追加する。
  - `ReviewListViewModelTest` / UI test を個性表示へ更新する。

- Android/Compose tests
  - 酒リストの平均評価 contentDescription と表示値を確認する。
  - レビューリストで香り chip が最大4件表示され、上立ち香2件・含み香2件が存在することを確認する。
  - レビュー編集で BASIC 以外のタブに必須注記が出ないこと、タイトルに `銘柄:` が出ないことを確認する。

- Verification
  - 標準確認は `./gradlew localFix`。
  - migration や Compose test の失敗診断が必要な場合のみ、対象テストを補助的に実行する。

## Assumptions

- アルコール小数は保存精度を `Float?` とし、表示・再編集時は既存の `toString()` ベースで `16.5` のように出す。
- 平均評価は `OverallReview.ordinal + 1` を点数として扱い、未評価レビューは平均母数から除外する。評価が1件もない場合は酒リストでは `-` を表示する。
- カメラ撮影画像は現行実装どおり `.jpg` ファイルとして作成される前提で、MIME 未取得時の拡張子 fallback で受け入れる。
