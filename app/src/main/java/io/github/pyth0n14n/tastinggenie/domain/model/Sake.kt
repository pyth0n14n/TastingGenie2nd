package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade

/**
 * 酒の静的情報。
 */
data class Sake(
    val id: SakeId,
    val name: String,
    val grade: SakeGrade,
    val type: List<SakeClassification> = emptyList(),
    val typeOther: String? = null,
    val maker: String? = null,
    val prefecture: Prefecture? = null,
    val alcohol: Int? = null,
    val kojiMai: String? = null,
    val kojiPolish: Int? = null,
    val kakeMai: String? = null,
    val kakePolish: Int? = null,
    val sakeDegree: Float? = null,
    val acidity: Float? = null,
    val amino: Float? = null,
    val yeast: String? = null,
    val water: String? = null,
)

/**
 * 酒登録/編集の入力値。
 */
data class SakeInput(
    val id: SakeId? = null,
    val name: String,
    val grade: SakeGrade,
    val type: List<SakeClassification> = emptyList(),
    val typeOther: String? = null,
    val maker: String? = null,
    val prefecture: Prefecture? = null,
    val alcohol: Int? = null,
    val kojiMai: String? = null,
    val kojiPolish: Int? = null,
    val kakeMai: String? = null,
    val kakePolish: Int? = null,
    val sakeDegree: Float? = null,
    val acidity: Float? = null,
    val amino: Float? = null,
    val yeast: String? = null,
    val water: String? = null,
)
