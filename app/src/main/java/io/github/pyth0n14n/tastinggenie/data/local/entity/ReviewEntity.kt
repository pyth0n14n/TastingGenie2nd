package io.github.pyth0n14n.tastinggenie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness

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
    val scene: String?,
    val dish: String?,
    val appearanceSoundness: ReviewSoundness,
    val appearanceColor: SakeColor?,
    val appearanceViscosity: Int?,
    val aromaSoundness: ReviewSoundness,
    val aromaIntensity: IntensityLevel?,
    val aromaExamples: List<Aroma>,
    val aromaMainNote: String?,
    val aromaComplexity: ComplexityLevel?,
    val tasteSoundness: ReviewSoundness,
    val tasteAttack: AttackLevel?,
    val tasteTextureRoundness: TextureRoundness?,
    val tasteTextureSmoothness: TextureSmoothness?,
    val tasteMainNote: String?,
    val tasteSweetness: TasteLevel?,
    val tasteSourness: TasteLevel?,
    val tasteBitterness: TasteLevel?,
    val tasteUmami: TasteLevel?,
    val tasteInPalateAroma: List<Aroma>,
    val tasteAftertaste: TasteLevel?,
    val tasteComplexity: ComplexityLevel?,
    val otherIndividuality: String?,
    val otherCautions: String?,
    val otherOverallReview: OverallReview?,
)
