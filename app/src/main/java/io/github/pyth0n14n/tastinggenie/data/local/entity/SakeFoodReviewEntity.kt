package io.github.pyth0n14n.tastinggenie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature

@Entity(
    tableName = "sake_food_reviews",
    foreignKeys = [
        ForeignKey(
            entity = SakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["sakeId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["sakeId"]), Index(value = ["dateEpochDay"])],
)
data class SakeFoodReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sakeId: Long,
    val dateEpochDay: Long,
    val bar: String?,
    val dish: String?,
    val foodCompatibility: FoodCompatibility? = null,
    val temperature: Temperature?,
    val freeComment: String? = null,
)
