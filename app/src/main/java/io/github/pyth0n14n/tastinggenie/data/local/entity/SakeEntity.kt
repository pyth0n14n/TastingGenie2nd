package io.github.pyth0n14n.tastinggenie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade

@Entity(tableName = "sakes")
data class SakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val grade: SakeGrade,
    val type: List<SakeClassification>,
    val typeOther: String?,
    val maker: String?,
    val prefecture: Prefecture?,
    val alcohol: Int?,
    val kojiMai: String?,
    val kojiPolish: Int?,
    val kakeMai: String?,
    val kakePolish: Int?,
    val sakeDegree: Float?,
    val acidity: Float?,
    val amino: Float?,
    val yeast: String?,
    val water: String?,
)
