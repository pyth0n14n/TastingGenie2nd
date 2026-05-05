package io.github.pyth0n14n.tastinggenie.data.local.converter

import androidx.room.TypeConverter
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness

class ReviewPairingConverters {
    @TypeConverter
    fun toFoodCompatibility(raw: String?): FoodCompatibility? = raw?.let { enumValueOf<FoodCompatibility>(it) }

    @TypeConverter
    fun fromFoodCompatibility(value: FoodCompatibility?): String? = value?.name

    @TypeConverter
    fun toSweetDryness(raw: String?): SweetDryness? = raw?.let { enumValueOf<SweetDryness>(it) }

    @TypeConverter
    fun fromSweetDryness(value: SweetDryness?): String? = value?.name
}
