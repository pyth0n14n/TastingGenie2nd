# PLAN.md 更新案: 次開発フェーズ実行計画

## Summary
- 現在の blocker は `Gradle` ではなく `AGP 8.7.3 + compileSdk 36` の組み合わせ。`./gradlew ciCheck` は通るが、`compileSdk 36` 未検証警告が出るため、最初の PR はツールチェーン調整スパイクにする。
- 以降は大きな横断 PR を避け、**1 PR = 1機能責務**で進める。各 PR で `docs/spec` と `docs/spec/qa.md` を同時更新する。`PLAN.md` の旧前提「削除なし / 画像は Review / JSON のみ」は破棄する。
- 画像は **1酒1画像**、入力は **SakeEdit から**、保存は **アプリ専用領域**、バックアップは **ZIP（manifest JSON + 画像ファイル）** をデフォルトとする。

## Public API / Interface / Type 変更
- `Sake` / `SakeInput` に `imageUri: String?` を追加し、画像の正規保存先を酒に移す。
- `Review` / `ReviewInput` から `imageUri` を削除する。
- `ReviewEntity` から `imageUri` カラムを削除し、Room migration で整理する。
- `SakeRepository` に `deleteSake(id)`、`ReviewRepository` に `deleteReview(id)` を追加する。
- Backup I/O は `.json` 単体から `.zip` に変更し、ZIP 内に `backup.json` と `images/...` を持つ構成へ変更する。
- `prefecture` は地方 -> 都道府県の階層選択 UI を前提化し、`classification` は `OTHER` 時に自由記述欄を必須表示にする。
- `viscosity` は他の段階評価に合わせて `1..5` に統一する。

## PR Plan
1. **PR-1: Toolchain compatibility spike**
   - AGP / Kotlin / KSP の更新を 1 回試行し、Android Studio 推奨警告の解消を目指す。
   - 成功時は更新を採用。失敗時は依存ブロッカーを記録し、警告抑止または compileSdk 方針を docs に固定して終了する。
   - 完了条件: Android Studio sync と `./gradlew ciCheck` が通る。

2. **PR-2: 共通入力コンポーネント刷新**
   - 既存 `SimpleDropdown` を見直し、ポップアップのアンカーずれを解消する。
   - 日付 picker、段階選択、階層選択、確認ダイアログの共通 UI 土台を追加する。
   - 完了条件: 左端にずれるポップアップがなくなり、後続 PR がこの基盤だけで実装できる。

3. **PR-3: 酒フォーム拡張 前半**
   - `classification` 複数選択 + `OTHER` 自由記述、`maker`、`prefecture` 階層選択を追加する。
   - 種別選択 UI をタップしやすい形に置き換える。
   - 完了条件: 既存保存・編集・再表示で上記項目が往復する。

4. **PR-4: 酒フォーム拡張 後半**
   - `sakeDegree`、`acidity`、`kojiMai`/`kojiPolish`、`kakeMai`/`kakePolish`、`alcohol`、`yeast`、`water` を追加する。
   - 既存 `amino` は削除せず現状維持とし、今回の要求対象外に置く。
   - 完了条件: SakeEdit が現行ドメイン項目をほぼ UI から編集可能になる。

5. **PR-5: 酒画像保存基盤**
   - `Sake.imageUri` 追加、`Review.imageUri` 削除、Room migration、アプリ専用領域への画像コピー、差し替え時の旧画像削除を実装する。
   - 入力は Photo Picker、表示は URI ベースのまま扱う。
   - 完了条件: 画像が再起動後も表示され、権限切れで壊れない。

6. **PR-6: 酒画像 UI**
   - SakeEdit に画像登録・差し替え・削除 UI を追加する。
   - 削除は即時ではなく確認導線を入れる。
   - 完了条件: 酒編集画面だけで画像の追加/削除が完結する。

7. **PR-7: 酒リストカード UI**
   - SakeList を 2 列カード表示に変更する。
   - カードには 画像 + 銘柄 + 種別 を表示する。
   - 画像未登録時の placeholder を用意し、`setting_image_preview` が無効なときは画像を出さない。
   - 完了条件: 一覧画面だけで酒画像を参照でき、モバイル幅でも崩れない。

8. **PR-8: Backup ZIP 対応**
   - Settings の import/export を ZIP ベースに変更し、`backup.json + images/` の round-trip を実装する。
   - 画像は ZIP 内相対パスで管理する。
   - 完了条件: 酒 + レビュー + 酒画像の export/import round-trip が成立する。

9. **PR-9: レビュー削除 UI**
   - ReviewList にゴミ箱導線と確認ダイアログを追加する。
   - 削除後の一覧再読込とエラー表示を整える。
   - 完了条件: 1レビュー削除が安全に完了し、誤タップで消えない。

10. **PR-10: 酒削除 UI**
   - SakeList にゴミ箱導線と確認ダイアログを追加する。
   - 酒削除時は紐づくレビューと酒画像を一括削除し、確認文に件数を出す。
   - 完了条件: cascade delete が整合性を壊さず完了する。

11. **PR-11: レビュー入力 UI 改修 前半**
    - 日付を DatePicker 化し、`YYYY-MM-DD` は UI が自動生成する。
    - 温度・色など残存 dropdown の配置問題を共通基盤へ寄せて解消する。
    - 完了条件: 手入力日付が不要になり、誤入力と左寄りポップアップが消える。

12. **PR-12: レビュー入力 UI 改修 後半**
    - `viscosity`、香味強度、甘味などの段階値を dropdown から段階選択 UI に変更する。
    - 総合評価を星 UI + 意味ラベル表示へ変更する。
    - 完了条件: レビューの主要評価値がタップしやすい連続選択 UI になる。

13. **PR-13: 画面遷移ちらつき修正**
    - XML theme、window background、Compose root background、ナビゲーション遷移時の空白描画を揃える。
    - 完了条件: 一覧/編集/設定/詳細の往復で白フラッシュが再現しない。

## Test Plan
- Toolchain: Android Studio sync、`./gradlew ciCheck`、必要なら `--warning-mode all` で deprecation 原因確認。
- 酒フォーム: `OTHER` 選択時入力欄表示、prefecture 階層選択、数値項目保存/再編集、必須項目バリデーション。
- 画像: pick -> 保存 -> 再起動後表示、差し替えで旧画像削除、画像削除確認、酒削除で画像も消える。
- Backup: ZIP export/import round-trip、画像付き復元、破損 ZIP / schema 不一致 / 欠損画像で明示エラー。
- 削除: review 単体削除、sake cascade delete、確認キャンセル、削除失敗時 UI エラー。
- レビュー UI: DatePicker 反映、段階選択の再編集、星評価ラベル表示、香りカテゴリ展開・複数選択。
- Migration: `Review.imageUri` 削除後に既存テスト DB / schema / mapper / import-export が整合すること。
- Flicker: 実機または emulator で `SakeList -> SakeEdit -> ReviewList -> ReviewEdit -> Settings` を往復して白フラッシュを手動確認。

## Assumptions / Defaults
- 未 launch のため後方互換は不要とし、`Review.imageUri` は互換保持なしで削除してよい。
- 各 PR は docs 更新込みで完結させ、巨大な先行 docs-only PR は作らない。
- `viscosity` は他の段階評価と同じ 5 段階で扱う。
- 旧 JSON backup は読み捨てではなく、互換が重ければ「非対応化を明記して終了」でよい。無理な後方互換は入れない。
- 仕様差分が出たら同一 PR で `docs/spec` と `docs/spec/qa.md` を更新する。
