# TastingGenie 開発開始プラン（仕様準拠・実装着手用）

## Summary
- 開発は **仕様補完 → 基盤実装 → 縦切りMVP（S0〜S4）→ v1残機能（S5/S6/S7 + JSON入出力）** の順で進める。
- 既決定事項を前提に固定する。
- 既決定事項:
  - 第1マイルストーン: `S0〜S4` の縦切りMVP
  - 第2マイルストーン: インポート/エクスポート（`JSONのみ`）
  - 未定義Enum: `5段階`
  - DI: `Hilt`
  - 削除機能: 第1マイルストーンでは含めない
  - マスタデータ: `JSON assets` 読み込み
  - 画像: `Reviewに1枚`

## Public API / Interface / Type 変更（先に固定）
- Domain types（新規）
  - `SakeId`, `ReviewId`（typealias Long）
  - `Sake`, `Review` ドメインモデル
  - `UiError`（メッセージID + optional cause key）
- Enum（specへ明記して実装）
  - `IntensityLevel`: `VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG`
  - `TasteLevel`: `VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG`
  - `OverallReview`: `VERY_BAD, BAD, NEUTRAL, GOOD, VERY_GOOD`
  - `Prefecture`: 47都道府県固定Enum（JIS順）
- Repository interfaces（新規）
  - `SakeRepository`
    - `observeSakes(): Flow<List<Sake>>`
    - `getSake(id: SakeId): Sake?`
    - `upsertSake(input: SakeInput): SakeId`
  - `ReviewRepository`
    - `observeReviews(sakeId: SakeId): Flow<List<Review>>`
    - `getReview(id: ReviewId): Review?`
    - `upsertReview(input: ReviewInput): ReviewId`
  - `MasterDataRepository`
    - `getMasterData(): MasterDataBundle`（assets JSONをメモリキャッシュ）
- Room schema（初版）
  - `sakes` テーブル
  - `reviews` テーブル（`sakeId` FK, `imageUri TEXT NULL`）
  - List/Enum は `TypeConverter` で保存（JSON string）
- Navigation routes（新規）
  - `sake/list` (S0)
  - `sake/edit/{sakeId?}` (S1)
  - `review/list/{sakeId}` (S2)
  - `review/edit/{sakeId}/{reviewId?}` (S3)
  - `review/detail/{reviewId}` (S4)
  - M2で追加: `review/image/{reviewId}` (S5), `help` (S6), `settings` (S7)

## 実行計画（PR単位, 1PR=1責務）

### PR-1: Spec補完（docsのみ）
- 目的: 実装判断をゼロにする。
- 変更:
  - `docs/spec/data_model.md` に未定義Enumの定義追記。
  - `docs/spec/master/` に不足マスタ追加:
    - `prefecture.md`
    - `intensity.md`
    - `taste_scale.md`
    - `overall_review.md`
    - `import_export.md`（M2でJSON、version付き）
    - `image.md`（Review 1枚）
- 完了条件:
  - docs内で未定義項目がなく、実装者が追加判断なしで着手可能。

### PR-2: アーキテクチャ基盤（Hilt + Room + MasterData）
- 目的: 以降の画面実装で共通利用する土台を先に固定。
- 変更:
  - 依存追加（Hilt, Room, Navigation Compose, kotlinx-serialization）
  - `Application` + Hilt module
  - `AppDatabase`, DAO, Entity, Converter
  - `RepositoryImpl`（DB + assets読み込み）
  - `assets/master/*.json` 作成（既存master仕様分）
- 完了条件:
  - unit testで DB CRUD と converter が通る。
  - `./gradlew ciCheck` 通過。

### PR-3: 縦切りMVP 前半（S0/S1 酒管理）
- 目的: 酒の登録と一覧の最短価値を提供。
- 変更:
  - `S0` 酒一覧（空/読込/エラー状態）
  - `S1` 酒登録/編集（必須: name, grade）
  - `SakeListViewModel`, `SakeEditViewModel`（StateFlow）
- 完了条件:
  - 酒の新規登録・編集・一覧反映・再起動後保持が成立。
  - UI文字列は `stringResource` 経由のみ。

### PR-4: 縦切りMVP 後半（S2/S3/S4 レビュー管理）
- 目的: 同一銘柄への複数レビュー記録を完成。
- 変更:
  - `S2` レビュー一覧
  - `S3` レビュー登録/編集（必須: date）
  - `S4` レビュー詳細
  - 味覚/香り/温度/色などマスタ選択UI
- 完了条件:
  - 1銘柄に複数レビュー保存可能。
  - 作成/編集/詳細/一覧の往復で整合性維持。
  - 削除機能は未実装（仕様通りM1スコープ外）。

### PR-5: v1機能追加（S5 + S6 + S7）
- 目的: v1に含む残画面を実装。
- 変更:
  - `S5` 単一画像ビューア（Review.imageUri表示）
  - `S6` ヘルプ（用語表示）
  - `S7` 設定（表示・画像設定の最小要件）
- 完了条件:
  - 画像URI表示と画面遷移が動作。
  - 設定値は DataStore で保持。

### PR-6: JSONインポート/エクスポート（M2）
- 目的: データ持ち運び対応。
- 変更:
  - JSON schema version (`schemaVersion`) 定義
  - export: `sakes + reviews` を1ファイル出力
  - import: バリデーション後 upsert
  - 失敗は UI state で明示（例外握り潰し禁止）
- 完了条件:
  - export→importでデータ再現。
  - version不一致/破損JSONでユーザーに明示エラー。

## テストケースとシナリオ

- Unit
  - Repository: `upsertSake`, `upsertReview`, `observe*` の整合
  - Converter: Enum/List/LocalDate(またはepochDay) の往復
  - MasterDataRepository: assets JSON parse・必須キー欠損時エラー
- ViewModel
  - 初期読込、成功、入力エラー、保存失敗で `UiState` 遷移が正しい
- UI（Compose test）
  - S0→S1→S0 登録反映
  - S0→S2→S3→S2 レビュー反映
  - S2→S4 詳細表示
- E2E（手動受け入れ）
  - 同一銘柄へ複数レビュー登録
  - アプリ再起動後の保持
  - M2: JSON export/import round-trip

## Assumptions / Defaults
- 第1マイルストーンで削除機能は実装しない。
- 画像は Review に1件のみ保持（`imageUri: String?`）。
- マスタデータは assets JSONを単一ソースとし、DBマスタテーブルは作らない。
- import/export は第2マイルストーンで JSONのみ。
- CIゲートは現状どおり `./gradlew ciCheck` を必須とする。
- 仕様と実装差分が出た場合は同一PRで `docs/spec` を更新する。
