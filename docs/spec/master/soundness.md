# 健全度マスタ

## 対象項目
- `appearanceSoundness`
- `aromaSoundness`
- `tasteSoundness`

## 選択方式
- 2段階の単一選択 UI
- 対象項目はいずれも任意（未選択は `null`）
- レビュー入力で表示する場合、新規入力とクリア後は未選択にする
- `showReviewSoundness = false` のときは UI を非表示にし、レビュー編集ロード時に保存値を `SOUND` に初期化する

| value | 表示名 |
|--------|--------|
| SOUND | 健全 |
| UNSOUND | 不健全 |
