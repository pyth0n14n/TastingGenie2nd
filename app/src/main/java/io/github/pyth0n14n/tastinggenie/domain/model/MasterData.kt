package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.AromaGroup

/**
 * 単一選択/複数選択の共通マスタ項目。
 */
data class MasterOption(
    val value: String,
    val label: String,
    val description: String? = null,
)

/**
 * 香りカテゴリ。
 */
data class AromaCategoryMaster(
    val group: AromaGroup,
    val label: String,
    val items: List<MasterOption>,
)

/**
 * アプリで使用するマスタデータ一式。
 */
data class MasterDataBundle(
    val sakeGrades: List<MasterOption>,
    val classifications: List<MasterOption>,
    val temperatures: List<MasterOption>,
    val colors: List<MasterOption>,
    val prefectures: List<MasterOption>,
    val intensityLevels: List<MasterOption>,
    val tasteLevels: List<MasterOption>,
    val overallReviews: List<MasterOption>,
    val aromaCategories: List<AromaCategoryMaster>,
)
