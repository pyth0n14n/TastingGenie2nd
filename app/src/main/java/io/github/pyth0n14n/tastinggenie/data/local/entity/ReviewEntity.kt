package io.github.pyth0n14n.tastinggenie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = SakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["sakeId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["sakeId"])],
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sakeId: Long,
    val dateEpochDay: Long,
    val bar: String?,
    val price: Int?,
    val volume: Int?,
    val temperature: Temperature?,
    val color: SakeColor?,
    val viscosity: Int?,
    val intensity: IntensityLevel?,
    val scentTop: List<Aroma>,
    val scentBase: List<Aroma>,
    val scentMouth: List<Aroma>,
    val sweet: TasteLevel?,
    val sour: TasteLevel?,
    val bitter: TasteLevel?,
    val umami: TasteLevel?,
    val sharp: TasteLevel?,
    val scene: String?,
    val dish: String?,
    val comment: String?,
    val review: OverallReview?,
    val imageUri: String?,
)
