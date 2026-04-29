# UI Improvement Plan: Figma-driven Material Design 3 Alignment

## Summary
- 既存 PLAN は完了済みとして今後の計画対象から外す。
- UI 改善は Figma `Tasting genie 2nd` を画面単位の正とし、Material Design 3 準拠を目標に進める。
- 第1波は Figma が存在する `S0: Sake list`、`S1: Sake register`、`S2: Review list`、`S3: Review register` に限定する。
- `S4: Review detail`、画像ビューア、ヘルプ、設定は未設計または未確定のため、この PLAN では現状維持または後続 PLAN 扱いにする。

## PR1: `feat(ui-foundation-m3)`
- Figma で確認した light scheme を Compose theme に反映する。
- Dynamic color は Figma 準拠画面の基準から外し、固定 light color scheme を標準にする。
- `titleLarge`、`bodyLarge`、`bodyMedium`、`bodySmall`、`labelLarge`、`labelMedium`、`labelSmall` など、Figma で使う M3 typography ramp を定義する。
- 今後の画面差し替え用に、共通 app bar、section title、fixed bottom primary action、overflow menu の小さな共通部品を追加する。
- `docs/style/UI_DESIGN_CONSTRAINTS.md` を新設し、Figma 参照方法、未設計画面の扱い、許容される最小変更範囲を明文化する。
- `AGENTS.md` に UI 改善時の Figma-first ルールを追記する。

## PR2: `feat(s0-sake-list-figma-pass1)`
- `S0: Sake list` を Figma の 1 列 list へ寄せる。
- 現行の 2 列 card grid から、thumbnail、sake/type chip、maker、prefecture、latest rating を持つ list item へ置換する。
- 検索バーを追加し、銘柄名・酒造名の最小絞り込みを実装する。
- sort action は既存並び順との互換を優先し、限定的な sort mode から開始する。
- 既存の edit/delete/pin は前面表示から overflow menu へ集約する。
- 行タップは既存どおり review list への遷移を維持する。

## PR3: `feat(s1-sake-register-figma-pass1)`
- `S1: Sake register` を Figma の `画像`、`基本情報`、`詳細情報` セクション構成へ寄せる。
- 画像は横スクロールの追加カード + 既存画像パック + delete action に見た目を寄せる。
- 既存の複数画像保存、URI 管理、保存時取り込み、削除確認は維持する。
- フォームは 360dp 幅基準で 2 カラム配置を採用し、狭い表示や長文では破綻しないよう responsive fallback を入れる。
- validation、保存、戻る、既存データ互換は維持する。

## PR4: `feat(s2-review-list-figma-pass1)`
- `S2: Review list` に Figma の統計領域を追加する。
- review count と average overall rating を表示する。
- review list item の密度、余白、rating、日付表示を Figma に寄せる。
- 削除と画像表示は overflow または副次 action に集約し、既存操作は失わない。
- 集計は schema 変更なしの query/state 追加で実装する。

## PR5: `feat(s3-review-register-figma-pass1)`
- `S3: Review register` を Figma の text tab、connected button group、outlined text field、clear action、fixed save button に寄せる。
- 既存の `HorizontalPager`、section state、validation failure 時の基本情報復帰、save flow は維持する。
- icon-only tabs は Figma に合わせて text tabs へ戻す。
- Dropdown/slider を置換する場合も、保存される enum/master 値は既存互換を維持する。
- `基本情報`、`見た目`、`香り`、`味`、`特記事項` の各 section を画面単位で確認しながら進める。

## Later
- `S4: Review detail` は Figma 設計が確定してから別 PR で扱う。
- 画像ビューア、ヘルプ、設定は Figma frame 作成後に別 PLAN へ切り出す。
- dark theme は Figma の dark design ができるまで追加設計対象にしない。

## Test Plan
- Standard local verification: `./gradlew localFix`
- PR1: theme が compile でき、既存画面が固定 light scheme で表示されること。
- PR2: Sake list の検索、sort、row tap、overflow edit/delete/pin が動くこと。
- PR3: Sake edit の入力、validation、複数画像追加/削除、保存が既存どおり動くこと。
- PR4: Review list の件数・平均評価が review 追加/削除に追従すること。
- PR5: Review edit の section 切替、clear、validation、保存が既存どおり動くこと。

## Assumptions
- Figma MCP の呼び出し上限があるため、実装時は対象 frame 単位で取得し、取得結果を PR ごとに記録する。
- `S0` の Figma にある leading back icon は start destination としては採用せず、現行 navigation と整合する app bar に調整する。
- UI 改善 wave では DB schema 変更を避ける。必要な追加情報は query、repository、UiState で吸収する。
- Figma に見えていない既存操作は削除せず、overflow menu などへ移す。
