package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOptionGroup

fun List<MasterOption>.toOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.value,
            label = option.label,
        )
    }

fun List<MasterOption>.selectedLabel(value: String?): String =
    firstOrNull { option -> option.value == value }?.label.orEmpty()

fun List<MasterOption>.toClassificationGroups(): List<DropdownOptionGroup> =
    toDropdownGroups(CLASSIFICATION_GROUP_DEFINITIONS)

fun List<MasterOption>.toPrefectureGroups(): List<DropdownOptionGroup> = toDropdownGroups(PREFECTURE_GROUP_DEFINITIONS)

private fun List<MasterOption>.toDropdownGroups(
    definitions: List<DropdownGroupDefinition>,
): List<DropdownOptionGroup> {
    val optionsByValue = associateBy { option -> option.value }
    return definitions.mapNotNull { definition ->
        val groupedOptions =
            definition.optionValues.mapNotNull { value ->
                optionsByValue[value]
            }
        if (groupedOptions.isEmpty()) {
            null
        } else {
            DropdownOptionGroup(
                label = definition.label,
                options = groupedOptions.toOptions(),
            )
        }
    }
}

private data class DropdownGroupDefinition(
    val label: String,
    val optionValues: List<String>,
)

private val CLASSIFICATION_GROUP_DEFINITIONS =
    listOf(
        DropdownGroupDefinition(label = "酛", optionValues = listOf("KIMOTO", "YAMAHAI")),
        DropdownGroupDefinition(
            label = "火入れ",
            optionValues = listOf("NAMAZUME", "NAMACHOZO", "NAMA"),
        ),
        DropdownGroupDefinition(label = "加水", optionValues = listOf("GENSHU")),
        DropdownGroupDefinition(
            label = "貯蔵",
            optionValues = listOf("HIYAOROSHI", "YUKIMURO", "TARU", "AGED"),
        ),
        DropdownGroupDefinition(label = "新旧", optionValues = listOf("SHINSHU", "KOSHU")),
        DropdownGroupDefinition(label = "濾し", optionValues = listOf("NIGORI", "ORI", "DOBUROKU")),
        DropdownGroupDefinition(
            label = "絞り",
            optionValues = listOf("ARABASHIRI", "NAKAGUMI", "SEME", "SHIZUKU"),
        ),
        DropdownGroupDefinition(label = "食感", optionValues = listOf("FROZEN", "SPARKLING")),
        DropdownGroupDefinition(label = "製造", optionValues = listOf("KIIPON", "KIJOSHU")),
        DropdownGroupDefinition(label = "その他", optionValues = listOf("OTHER")),
    )

private val PREFECTURE_GROUP_DEFINITIONS =
    listOf(
        DropdownGroupDefinition(label = "北海道", optionValues = listOf("HOKKAIDO")),
        DropdownGroupDefinition(
            label = "東北",
            optionValues = listOf("AOMORI", "IWATE", "MIYAGI", "AKITA", "YAMAGATA", "FUKUSHIMA"),
        ),
        DropdownGroupDefinition(
            label = "北関東",
            optionValues = listOf("IBARAKI", "TOCHIGI", "GUNMA", "YAMANASHI", "NAGANO"),
        ),
        DropdownGroupDefinition(
            label = "南関東",
            optionValues = listOf("SAITAMA", "CHIBA", "TOKYO", "KANAGAWA"),
        ),
        DropdownGroupDefinition(label = "東海", optionValues = listOf("SHIZUOKA", "GIFU", "AICHI", "MIE")),
        DropdownGroupDefinition(label = "北陸", optionValues = listOf("NIIGATA", "TOYAMA", "ISHIKAWA", "FUKUI")),
        DropdownGroupDefinition(
            label = "近畿",
            optionValues = listOf("SHIGA", "KYOTO", "OSAKA", "HYOGO", "NARA", "WAKAYAMA"),
        ),
        DropdownGroupDefinition(
            label = "中国",
            optionValues = listOf("TOTTORI", "SHIMANE", "OKAYAMA", "HIROSHIMA", "YAMAGUCHI"),
        ),
        DropdownGroupDefinition(
            label = "四国",
            optionValues = listOf("TOKUSHIMA", "KAGAWA", "EHIME", "KOCHI"),
        ),
        DropdownGroupDefinition(
            label = "九州",
            optionValues =
                listOf(
                    "FUKUOKA",
                    "SAGA",
                    "NAGASAKI",
                    "KUMAMOTO",
                    "OITA",
                    "MIYAZAKI",
                    "KAGOSHIMA",
                ),
        ),
        DropdownGroupDefinition(label = "沖縄", optionValues = listOf("OKINAWA")),
    )
