# ききさけ帖 テスト・チェックリスト

## 概要
内部テスト、クローズドテストのための動作チェックリスト。  
重要度は、P0 > P1 > P2 で、クローズドではあまり負荷にならない範囲で作業をお願いする見込み。

## テスト方法の凡例

| 記号 | 意味 |
| -- | -- |
| ◎ | 主にこのテストで実施すべき |
| ○ | 実施可能 |
| △ | 一部可能。Fake / Stub / Test seam が必要、または完全代替は困難 |
| × | 基本的に不向き。手動確認を残すべき |

`実施` は、既存の unitTest / androidTest が少なくとも一部をカバーしているものを ☑ とする。☐ は未自動化、または現状の既存テストでは対象項目としては不足しているもの。

## 自動化判定レビュー

- 判定方針はおおむね妥当。CRUD、入力検証、Repository、ZIP import/export、画像 MIME/サイズ判定は unitTest 寄せ、画面表示・Dialog・タブ・Navigation は androidTest 寄せでよい。
- 料理相性レビュー検証は自動化済み。画面では「料理との相性 *」で必須表示され、`SakeFoodReviewEditViewModel.save()` でも `date` / `dish` / `foodCompatibility` を必須として検証する。
- SAF、写真選択、カメラ、権限、IME、視認性は自動化しても完全代替にはならない。クローズド前は手動確認を残す。
- 既存テストは ViewModel / Repository / Compose screen 単体が中心。初回起動の空状態は androidTest を追加済み。画面間遷移と新規登録から一覧反映の E2E androidTest は引き続き追加候補。

## チェックリスト

**P0**
| 実施 | 項目 | 手動確認する挙動 | テスト方法 |
|---|---|---|---|
| ☑ | 初回起動 | 新規インストール直後にクラッシュせず、酒一覧の空状態が表示される | androidTest ◎ / UnitTest △。Activity起動と空状態表示は androidTest。`MainActivityLaunchTest` で初回空状態を確認。初期Stateのみ UnitTest 可。 |
| ☑ | 酒の新規登録 | 銘柄名、種別を入力して保存でき、酒一覧に戻って表示される | androidTest ◎ / UnitTest ○。UI入力から一覧反映は androidTest。保存ロジックと Repository insert は既存 UnitTest あり。 |
| ☑ | 酒の入力バリデーション | 銘柄名未入力、種別未選択、不正な数値で保存できず、エラーが出る | UnitTest ◎ / androidTest ○。`SakeEditViewModelTest` と form row test で既存カバーあり。エラー表示は androidTest 追加余地あり。 |
| ☑ | 酒の編集 | 登録済みの銘柄、分類、酒造、都道府県、数値項目を編集して保存後も反映される | androidTest ◎ / UnitTest ○。既存 UnitTest で edit load、更新、詳細項目保存を確認済み。画面往復は androidTest 追加候補。 |
| ☑ | 酒の削除 | 削除確認が出る。削除後、関連レビューも一覧から消える | androidTest ◎ / UnitTest ○。既存 androidTest で削除Dialog、UnitTest/Repository test で関連レビュー削除と画像cleanupを確認済み。 |
| ☑ | 酒一覧からレビュー一覧へ遷移 | 酒カードを開くと、その酒のレビュー一覧に遷移する | androidTest ◎ / UnitTest △。既存 androidTest はカードクリック callback まで。NavHost 実遷移は追加候補。 |
| ☑ | レビュー新規登録 | 酒レビュータブからレビューを追加し、日付・温度・外観・香り・味・特記を保存できる | androidTest ◎ / UnitTest ○。既存 UnitTest で ReviewEdit 保存・Mapper/Repository を確認済み。UI多項目入力は追加候補。 |
| ☑ | レビュー必須/数値検証 | 日付不正、価格範囲外、容量範囲外で保存できず、基本情報タブへ誘導される | UnitTest ◎ / androidTest ○。既存 UnitTest で日付・価格・容量検証あり。タブ誘導は androidTest 追加候補。 |
| ☑ | レビュー詳細 | 保存したレビューを開くと、日付、総合評価、温度、香り・味のサマリ、詳細項目が破綻なく表示される | androidTest ◎ / UnitTest △。既存 androidTest で summary、accordion、空セクション抑制を確認済み。表示整形は UnitTest 一部あり。 |
| ☑ | レビュー編集 | 詳細右上の編集から既存レビューを修正し、詳細・一覧に反映される | androidTest ◎ / UnitTest ○。既存 androidTest で詳細の編集導線、UnitTest で編集後refreshを確認済み。E2E更新は追加候補。 |
| ☑ | レビュー削除 | 削除確認が出て、削除後に一覧から消える | androidTest ◎ / UnitTest ○。既存 androidTest で削除Dialog、UnitTest/Repository test で削除ロジックを確認済み。 |
| ☑ | 料理相性レビュー登録 | 料理相性タブから追加し、日付、料理、料理との相性、温度、店名、コメントを保存できる | androidTest ◎ / UnitTest ○。`SakeFoodReviewEditViewModelTest` で保存、`ReviewListScreenTest` で料理相性タブの追加導線、`SakeFoodReviewEditScreenTest` で保存ボタンを確認。 |
| ☑ | 料理相性レビュー検証 | 日付・料理・料理との相性が未入力のとき保存できないことを確認する。現コード上、料理との相性は必須表示に見えるため、ここは特に要確認 | UnitTest ◎ / androidTest ○。`SakeFoodReviewEditViewModelTest` で料理・料理との相性の必須検証、`SakeFoodReviewEditScreenTest` でエラー表示を確認。 |
| ☑ | 料理相性レビュー編集/削除 | 既存の料理相性レビューを開いて編集・削除できる | androidTest ◎ / UnitTest ○。`SakeFoodReviewEditViewModelTest` で編集ロード/更新、`ReviewListViewModelTest` と `ReviewListScreenTest` で料理相性削除を確認。 |
| ☑ | 画像追加 | 酒編集でギャラリーから JPEG/PNG/WebP を追加し、保存後に酒一覧・レビュー一覧・画像ビューアで表示される | androidTest △ / UnitTest ○。実ギャラリーは避ける。既存 UnitTest で import/persist、既存 androidTest で画像ボタン・表示を一部確認済み。 |
| ☑ | カメラ撮影画像 | カメラで撮影して保存でき、保存後に画像が残る | androidTest △ / UnitTest △。実カメラは手動。既存 androidTest は撮影 callback、UnitTest は一時capture cleanupを確認済み。 |
| ☑ | 画像削除 | 画像削除時に確認ダイアログが出る。保存後、該当画像が消える | androidTest ◎ / UnitTest ○。既存 androidTest で確認Dialog、UnitTest で保存後削除/cleanupを確認済み。 |
| ☑ | 下書き破棄 | 酒編集・レビュー編集・料理相性編集で入力後に戻ると破棄確認が出る。キャンセルで画面に残り、確定で戻る | androidTest ◎ / UnitTest △。`SakeEditScreenTest`、`ReviewEditScreenTest`、`SakeFoodReviewEditScreenTest` で入力変更後の戻る、破棄Dialog、キャンセル、確定を確認済み。 |
| ☑ | 設定の永続化 | ヘルプ表示、健全度表示、レビュー入力モードを変更し、画面遷移・アプリ再起動後も反映される | androidTest ◎ / UnitTest ◎。既存 UnitTest で Settings ViewModel/Repository を確認済み。再起動込みは androidTest 追加候補。 |
| ☑ | 健全度非表示 | 設定で健全度を非表示にすると、レビュー入力・詳細で健全度が出ない | androidTest ◎ / UnitTest ○。既存 UnitTest で非表示時の保存値初期化、既存 androidTest で一部表示条件を確認済み。 |
| ☑ | バックアップ書き出し | 設定から ZIP バックアップを書き出せる。完了メッセージが出る | UnitTest ◎ / androidTest △。既存 UnitTest で ZIP 生成、SettingsViewModel 成功/失敗を確認済み。SAF実UIは手動。 |
| ☑ | バックアップ復元 | 既存データがある状態で復元し、バックアップ内容で置き換わる。マージされない | UnitTest ◎ / androidTest △。既存 UnitTest で置換・非マージ・画像URI復元を確認済み。SAF実UIは手動。 |
| ☑ | 復元失敗時の保護 | 壊れた ZIP、非対応形式、JSON 旧形式などを読み込ませても既存データが消えない | UnitTest ◎ / androidTest ○。既存 UnitTest で各種失敗と既存データ保護、SettingsViewModel のエラー変換を確認済み。 |
| ☑ | 画像付きバックアップ | 画像付き酒をバックアップ・復元し、復元後も画像が表示される | UnitTest ◎ / androidTest △。既存 UnitTest で ZIP内画像と復元後URIを確認済み。表示確認は androidTest/手動。 |
| ☑ | アプリについて | 「20歳未満の飲酒禁止」表示が確認できる | androidTest ◎ / UnitTest ×。既存 `SettingsScreenTest` で文言表示を確認済み。 |
| ☑ | 公開名 | ユーザー向け表示が「ききさけ帖」で、開発名が見えない | androidTest ◎ / UnitTest △。既存 androidTest で設定のアプリ名表示を一部確認済み。ランチャー名・全画面文言の静的検査は追加余地あり。 |

**P1**
| 実施 | 項目 | 手動確認する挙動 | テスト方法 |
|---|---|---|---|
| ☑ | 酒一覧検索 | 銘柄名・酒造名で検索でき、該当なし表示も出る | androidTest ◎ / UnitTest ○。既存 UnitTest と androidTest で検索を確認済み。該当なし文言は追加余地あり。 |
| ☑ | 酒一覧ソート | 登録順、銘柄名順、評価順が切り替わる | androidTest ◎ / UnitTest ◎。既存 UnitTest で登録順/評価順、Repository で並び順を確認済み。UI切替も一部確認済み。 |
| ☑ | ピン留め | 酒を固定/解除でき、ピン止めセクションに移動する | androidTest ◎ / UnitTest ○。既存 UnitTest で pin 状態、androidTest で overflow action とセクション表示を確認済み。 |
| ☑ | ピン止め/酒一覧セクション | セクションの開閉状態が自然に動く | androidTest ◎ / UnitTest △。既存 androidTest でセクション分離と開閉を確認済み。 |
| ☑ | 複数画像 | 1つの酒に複数画像を登録し、プレビュー・ビューアで確認できる | androidTest △ / UnitTest ○。既存 UnitTest で複数選択/保存の一部を確認済み。UIプレビューの複数枚確認は追加候補。 |
| ☑ | 同一画像の重複選択 | 同じ画像を複数回選んでも重複表示・保存されない | UnitTest ◎ / androidTest ○。既存 `onImageSelected_withDuplicateUri_keepsSinglePreviewAndSingleImport` あり。 |
| ☑ | 画像未登録 | 画像なし酒で placeholder と「画像が登録されていません」が自然に出る | androidTest ◎ / UnitTest △。既存 androidTest で酒一覧 placeholder と画像ビューア空状態を確認済み。 |
| ☑ | 非対応画像 | GIF/PDFなどを選んだ場合に対応外エラーが出る | UnitTest ◎ / androidTest △。既存 UnitTest で MIME rejection と ViewModel エラー変換を確認済み。Picker stub UIは追加候補。 |
| ☑ | 10MB超画像 | サイズ超過画像でエラーが出る | UnitTest ◎ / androidTest △。既存 UnitTest でサイズ超過と部分ファイル削除、ViewModel エラー変換を確認済み。 |
| ☑ | 画像編集キャンセル | 画像追加・削除後に保存せず戻った場合、既存データが変わらない | androidTest ◎ / UnitTest ○。`SakeEditViewModelImageCleanupTest` で画像追加・既存画像削除後に保存しない場合、永続化済み画像が変わらず、未保存の一時画像だけcleanupされることを確認済み。 |
| ☑ | レビュー入力モード 通常 | 通常モードで通常向け項目だけが表示される | androidTest ◎ / UnitTest ○。既存 UnitTest で通常モード定義を確認済み。UI表示差分は追加候補。 |
| ☑ | レビュー入力モード 利酒師 | 利酒師モードで記述系項目が表示され、通常モードとの差分が反映される | UnitTest ○ / androidTest ◎。`ReviewModeTest` と `ReviewEditViewModelTest` で利酒師モードの有効項目、記述系項目、通常モードとの差分を確認済み。UI表示差分は追加余地あり。 |
| ☑ | レビュー入力モード デバッグ | 全項目が表示される | androidTest ◎ / UnitTest ○。既存 UnitTest で debug モードが全項目を有効化することを確認済み。UI表示差分は追加候補。 |
| ☑ | ヘルプ表示トグル | ヘルプ表示 OFF でレビュー項目のヘルプ導線が消え、ON で戻る | androidTest ◎ / UnitTest ○。既存 androidTest で酒編集/レビュー編集のヘルプ表示切替、UnitTest で設定反映を確認済み。 |
| ☑ | レビュー詳細 accordion | 香り、味、基本情報、見た目、メモ・評価の開閉ができる | androidTest ◎ / UnitTest △。既存 androidTest で初期折り畳みと開閉を確認済み。 |
| ☑ | 未入力項目の詳細表示 | 未入力項目が詳細で過剰に表示されない | androidTest ◎ / UnitTest ○。既存 androidTest で空セクション非表示を確認済み。 |
| ☑ | 平均評価 | 複数レビュー登録時、酒一覧・レビュー一覧の平均評価が更新される | UnitTest ◎ / androidTest ◎。既存 UnitTest/Repository test と androidTest で平均評価を確認済み。 |
| ☑ | レビュー一覧タブ | 酒レビュー/料理相性タブを切り替え、FAB の追加先がタブに応じて変わる | androidTest ◎ / UnitTest △。`ReviewListScreenTest` で酒レビュー/料理相性タブ切替、空状態表示、FAB追加先の分岐を確認済み。 |
| ☑ | 設定中の転送状態 | バックアップ処理中に多重実行できず、「読み書き中」が出る | androidTest ◎ / UnitTest ○。既存 UnitTest で処理中/キャンセル/再表示時フィードバックを確認済み。UI多重操作は追加候補。 |
| ☑ | 復元後の設定 | バックアップ内の設定、レビュー入力モード、健全度表示が復元される | UnitTest ◎ / androidTest ○。既存 UnitTest で settings と reviewMode の export/restore を確認済み。画面反映は追加候補。 |
| ☑ | 用語集 | 設定から用語集に遷移し、戻れる | androidTest ◎ / UnitTest ×。既存 androidTest で設定行 callback を確認済み。NavHost の実遷移は追加候補。 |
| ☐ | 画面回転 | 入力中・バックアップ完了メッセージ・設定変更が回転で破綻しない | androidTest ◎ / UnitTest △。ViewModel state の一部は既存 UnitTest あり。実際の再構成テストは不足。 |

**P2**
| 実施 | 項目 | 手動確認する挙動 | テスト方法 |
|---|---|---|---|
| ☑ | 空状態文言 | 酒なし、レビューなし、料理相性なし、検索該当なしの文言が自然 | androidTest ◎ / UnitTest △。画像なし/一部空状態は既存テストあり。全空状態文言は追加余地あり。 |
| ☑ | 長文入力 | 銘柄名、酒造、コメント、香り/味の自由記述が長くてもレイアウトが崩れない | androidTest ◎ / UnitTest △。既存 androidTest で酒一覧タイトル高さの安定を確認済み。長文全般は手動確認を残す。 |
| ☐ | 日本語IME | 入力中の変換、改行、フォーカス移動が不自然でない | androidTest △ / UnitTest ×。自動化困難。基本は手動。 |
| ☐ | 小画面端末 | 画面下部の保存ボタン、FAB、ナビゲーションバーが重ならない | androidTest △ / UnitTest ×。保存ボタン表示の一部テストはあるが、小画面構成での確認は不足。手動を残す。 |
| ☐ | ダーク/ライト | 現在想定テーマで文字が読め、カードやボタンが破綻しない | androidTest △ / UnitTest ×。screenshot test 化は可能だが、現状は不足。視認性は手動を残す。 |
| ☐ | Android権限 | カメラ・写真選択の権限許可/拒否後にクラッシュしない | androidTest △ / UnitTest ×。UiAutomation等で可能だが脆い。実機手動を残す。 |
| ☐ | バックアップ保存キャンセル | ファイル保存先選択をキャンセルしてもエラー扱いにならない | androidTest △ / UnitTest ○。ActivityResult cancel stub で確認可能。現状は専用テスト不足。 |
| ☐ | 復元ファイル選択キャンセル | ファイル選択キャンセルで状態が変わらない | androidTest △ / UnitTest ○。ActivityResult cancel stub で確認可能。現状は専用テスト不足。 |
| ☑ | 復元エラー後の再操作 | 一度復元失敗したあと、正常ZIPで再復元できる | UnitTest ◎ / androidTest ○。既存 UnitTest でエラー/フィードバックのクリアと再表示制御を確認済み。連続UI操作は追加候補。 |
| ☐ | アプリ再起動 | 保存済みの酒、レビュー、画像、設定が再起動後も残る | androidTest ◎ / UnitTest ○。Repository/DataStore の永続化テストはあるが、アプリ再起動E2Eは不足。 |
| ☐ | 戻る連打/保存連打 | 連打しても二重保存・クラッシュしない | androidTest ◎ / UnitTest ○。保存中ボタン無効化の表示は一部あるが、連打耐性としては不足。 |
| ☑ | アクセシビリティ | 主要ボタン、画像、削除、ピン留め、星評価の読み上げラベルが最低限成立する | androidTest ○ / UnitTest △。既存 androidTest で contentDescription / semantics の一部を確認済み。全主要操作は追加余地あり。 |
