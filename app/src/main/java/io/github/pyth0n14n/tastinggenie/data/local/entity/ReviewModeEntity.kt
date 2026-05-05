package io.github.pyth0n14n.tastinggenie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "review_modes", primaryKeys = ["id"])
data class ReviewModeEntity(
    val id: String,
    val label: String,
    val isBuiltIn: Boolean,
)

@Entity(
    tableName = "review_mode_items",
    primaryKeys = ["modeId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = ReviewModeEntity::class,
            parentColumns = ["id"],
            childColumns = ["modeId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["modeId"])],
)
data class ReviewModeItemEntity(
    val modeId: String,
    val itemId: String,
    val isEnabled: Boolean,
)
