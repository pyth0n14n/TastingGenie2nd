# PR11 再編プラン: 軽量UI改善先行、画像所有見直しは最後

## Summary
- 3 PR に分けて順番に進める。各 PR は `実装 + テスト + spec/doc 更新` を同じ責務で閉じる。
- `app/src/main/res/drawable/eye.xml`, `nose.xml`, `tongue.xml` が Android resource として使えるため、review tab の custom icon 前提に戻す。
- 順序は `1. icon化 + sake一覧の視認性改善` → `2. review UX / OTHER欄 fix` → `3. 画像所有と cleanup 再設計`。
- `review aroma wheel` はこの再編対象から外し、後続 PR に送る。
- 実施済み: `PR11 feat(ui-icon-polish)`。残りは `PR12` と `PR13`。

## PR11: `feat(ui-icon-polish)`
- sake 一覧の top bar を icon button 化する。
- `Help` は `?`、`Settings` は歯車に置換する。
- `showHelpHints` を SakeList でも反映し、OFF 時は Help アイコンを非表示にする。
- sake card の操作を icon 化する。
- `Edit` は鉛筆、`Favorite` はハートに置換する。内部状態 `isPinned` と並び順はそのまま維持する。
- ハートは現状より小さくして、カードタイトルを圧迫しないサイズにする。
- sake card は「1件のまとまり」が分かる見た目へ寄せる。現行 card 構造を維持しつつ、枠感・余白・elevation を調整する。
- review の section tabs は icon 化する。
- `基本情報=Info`、`外観=eye.xml`、`香り=nose.xml`、`味わい=tongue.xml`、`その他=Notes` を採用する。
- icon-only UI にする代わりに content description / semantics は既存日本語ラベルを必ず残す。
- docs 更新は `docs/PLAN.md` と、必要なら `docs/spec/qa.md` の settings/reactive UI 観点まで。
- テストは SakeList screen/viewmodel と review tab 表示の UI テストを更新する。
- 状態: 実施済み。

## PR12: `fix(review-ux-polish)`
- review edit の save ボタンをスクロール末尾から外し、固定 footer に移す。
- fixed save bar は `Scaffold` の bottom area に置き、IME 表示中でも save 導線が消えない構成にする。
- review の section 切替を `HorizontalPager` で横スワイプ対応にする。
- 対象は `ReviewEdit` と `ReviewDetail` の両方に統一する。
- tab tap と pager swipe は双方向同期する。
- validation エラーで `BASIC` に戻す既存仕様は維持する。
- sake edit の `種別=OTHER` 自由記述欄は、常に分類 selector の直下に表示されるよう固定する。
- この不具合は表示順の回帰としてテストで固定する。画像欄の下へ落ちる再発を防ぐ。
- docs 更新は `docs/PLAN.md` と `docs/spec/qa.md`。
- テストは review save bar 常時表示、tab swipe、`OTHER` 欄位置の回帰を追加する。
- 状態: 実施済み。

## PR13: `feat(image-ownership-cleanup)`
- 方針はユーザー提示案を採用する。永続画像は「保存時にアプリ管理領域へ取り込み、その後はアプリが所有する」。
- gallery 選択画像は save 時に managed 領域へ copy する。
- camera 撮影画像は現行どおり一時 cache を使い、save 時に managed 化する。
- 既定動作では、画像差し替え時とレコード削除時は DB の `imageUri` 参照だけを更新し、旧 managed file は即削除しない。
- Settings に `未参照アプリ内画像を削除` ボタンを追加する。
- `AppSettings` に `autoDeleteUnusedImages: Boolean = false` を追加する。
- `autoDeleteUnusedImages=true` のときだけ、差し替え保存後またはレコード削除後に未参照 managed image cleanup を自動実行する。
- cleanup 対象は `sakes.imageUri` に存在しない app-managed URI のみ。外部 URI と参照中 URI は削除しない。
- repository/DAO には「参照中 imageUri 一覧取得」と「未参照 managed image cleanup」の責務を追加する。
- DataStore 設定追加は必要だが DB schema migration は不要とする。
- docs 更新は `docs/spec/data_model.md`、`docs/spec/master/image.md`、`docs/spec/qa.md`、`docs/PLAN.md`。
- テストは gallery/camera save、replace、delete、manual cleanup、auto cleanup、save failure rollback を追加する。

## Test Plan
- SakeList: Help setting OFF で help icon 非表示、settings icon は常時表示、heart icon の toggle で pin 状態が維持されること。
- SakeList: card の境界が視認でき、既存の open/edit/delete/favorite 操作が失われないこと。
- Review UI: `Info / eye / nose / tongue / notes` の tab icon が表示され、content description は日本語ラベルになること。
- Review UI: edit/detail の両方で tab tap と横スワイプが同期すること。
- Review edit: 長いフォームでも save ボタンが常に画面下に見え、保存中 disable 表示も維持されること。
- Sake edit: `OTHER` 自由記述欄が分類 selector 直下に表示され、画像欄の下へ移動しないこと。
- Image: gallery/camera とも save 後は managed URI が保存されること。
- Image: 画像差し替え時、既定設定では旧 managed file が即削除されず、DB 参照だけが更新されること。
- Image: record 削除時、`autoDeleteUnusedImages=false` では file が残ること。
- Image: manual cleanup 実行で未参照 managed file のみ削除され、参照中 file と外部 URI は消えないこと。
- Image: `autoDeleteUnusedImages=true` では削除/差し替え後に manual cleanup と同等の結果になること。
- Image: save failure 時は新規 import 済み orphan file を cleanup し、既存参照は壊さないこと。

## Assumptions
- review tab は icon-only で進める。可視テキストは外し、アクセシビリティ名で補う。
- review の横スワイプ対応は `ReviewEdit` と `ReviewDetail` の両方を対象にする。
- pin は機能としては「固定」のまま、見た目だけハートへ変更する。アクセシビリティ文言は固定/固定解除を維持する。
- `eye.xml / nose.xml / tongue.xml` は現行の vector drawable をそのまま利用し、追加変換作業は不要とする。
