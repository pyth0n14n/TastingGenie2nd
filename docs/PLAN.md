# DB再設計プラン: 利酒師モード対応レビュー項目

## Summary

- `docs/spec/new_data_model_raw.md` は下書き扱いにし、正式仕様は `docs/spec/data_model.md` と新規のレビュー項目仕様ドキュメントへ反映する。
- Review は引き続き 1 件 = 1 テイスティングノート、Room の `reviews` 1 テーブルで保持する。
- 既存レビューは新構成へ移行するが、旧レビュー評価項目は初期化する。`id / sakeId / date / bar / price / volume / temperature / dish` など基本情報だけ保持する。
- アプリモードは `AppSettings` に保存する。初期値は `NORMAL`、選択肢は `NORMAL / KIKISAKE_SHI`。
- 将来のカスタムモード用に DB 領域を予約するが、今回カスタム編集UIは作らない。

## Key Changes

- 仕様ドキュメントを整理する。
  - `data_model.md`: 新 Review 構造、AppSettings、CustomMode 予約領域、保存形式を最新化。
  - 新規 `docs/spec/review_items.md`: カテゴリ、項目、通常/利酒師での表示有無、型、選択肢、保存フィールドを一覧化。
  - 新規 `docs/spec/review_help_messages.md`: raw のヘルプメッセージを項目ID単位で保存。UI対応は後続。
  - `docs/spec/master/*.md`: 新しい選択肢を master source of truth として追加する。

- Review の主な新規・変更フィールドを以下で固定する。
  - `foodCompatibility: FoodCompatibility?`: 料理との相性。旧 `scene` は廃止し、旧値は移行しない。
  - `tasteTextureNote: String?`: 利酒師モードの「テクスチャ：自由記述」。
  - `tasteDescription: String?`: 利酒師モードの「具体的な味わい：自由記述」。
  - `tasteSweetDryness: SweetDryness?`: 甘辛度。
  - `tasteInPalateAromaIntensity: IntensityLevel?`: 含み香：強度。
  - `tasteInPalateAromaExamples: List<Aroma>`: 含み香：具体例。既存 `tasteInPalateAroma` の後継。
  - `tasteAftertasteLength: TasteLevel?`: 余韻：長さ。既存 `tasteAftertaste` の後継。
  - `tasteAftertasteNote: String?`: 余韻：記述。
  - `otherSakeTypes: List<FlavorProfileType>`: 日本酒4タイプ。既存の導出 `FlavorProfileType` を domain enum として保存可能にする。

- 既存フィールドは raw の意味に合わせて維持または改名する。
  - `aromaMainNote` は「上立ち香：主体となる香り」の自由記述として維持。
  - `aromaComplexity` は通常モードのみ表示。
  - `appearanceSoundness / aromaSoundness / tasteSoundness` は利酒師モードのみ表示し、非表示時も既定値 `SOUND`。
  - `tasteSweetness / tasteSourness / tasteUmami / tasteBitterness` は通常モードのみ表示。
  - `tasteTextureRoundness / tasteTextureSmoothness` は通常モードのみ表示。

- DB / import-export を更新する。
  - Room DB version を `10` へ上げ、`MIGRATION_9_10` で `reviews` を再作成する。
  - migration は既存レビューの基本情報のみコピーし、評価・所感項目は `NULL` / 空リスト / `SOUND` に初期化する。
  - backup schema version を `9` へ上げる。
  - legacy schema 3-8 は読み込み可能にし、新 schema へ変換する。ただし旧評価項目は初期化する。
  - CustomMode 予約用に `review_modes` などのテーブルを追加し、標準モードも seed 済み DB 定義として扱う。

- UI / state の表示制御を導入する。
  - `ReviewMode` と `ReviewItemDefinition` を domain または feature shared 層に置き、各項目に `visibleInModes` を持たせる。
  - `ReviewEdit` は `AppSettings.reviewModeId` を読み、DB の項目定義に従って表示有無を切り替える。
  - 設定画面にレビュー表示モード選択を追加する。
  - ヘルプメッセージはデータとして持つが、既存のヘルプUI連携は今回の必須範囲にしない。

## Test Plan

- `AppDatabaseMigrationTest`
  - v9 から v10 へ移行できる。
  - 基本情報は保持され、旧レビュー評価項目は初期化される。
  - 新列・予約テーブルが Room schema と一致する。

- `ImportExportRepositoryImplTest`
  - schema 9 の export/import が新フィールドを保持する。
  - schema 3-8 の import が成功し、旧評価項目を初期化する。
  - 不正 enum / 範囲外値はクラッシュではなく既存方針通り失敗として扱う。

- `SettingsRepositoryImplTest` / `SettingsViewModelTest`
  - `reviewMode` の初期値は `NORMAL`。
  - `NORMAL / KIKISAKE_SHI` の切替が保存・復元される。

- `ReviewEditViewModelTest`
  - モードごとに表示対象項目が変わる。
  - 非表示項目は保存時に既定値または未入力として扱われ、古い値が残らない。
  - 健全度は非表示時も `SOUND`。

- UI tests
  - 通常モードでは通常対象項目のみ表示。
  - 利酒師モードでは利酒師対象項目が表示され、通常専用項目は非表示。
  - 設定変更後に ReviewEdit / ReviewDetail の表示が反映される。

- 最終確認は `./gradlew localFix`。

## Assumptions

- 既存レビューの評価項目は意図的に初期化する。基本情報と酒との紐付けは保持する。
- アプリモードはユーザー設定であり、Review レコード自体には作成時モードを保存しない。
- カスタムモードは DB 領域だけ予約し、編集・適用UIは後続対応とする。
- `new_data_model_raw.md` は正式仕様に昇格させず、内容を整理して既存 spec へ統合する。
