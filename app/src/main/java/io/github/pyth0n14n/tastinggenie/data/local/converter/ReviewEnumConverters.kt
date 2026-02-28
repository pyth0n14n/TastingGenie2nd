package io.github.pyth0n14n.tastinggenie.data.local.converter

import androidx.room.TypeConverter
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature

class ReviewEnumConverters {
    @TypeConverter
    fun toTemperature(raw: String?): Temperature? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromTemperature(value: Temperature?): String? = value?.name

    @TypeConverter
    fun toColor(raw: String?): SakeColor? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromColor(value: SakeColor?): String? = value?.name

    @TypeConverter
    fun toIntensity(raw: String?): IntensityLevel? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromIntensity(value: IntensityLevel?): String? = value?.name

    @TypeConverter
    fun toTaste(raw: String?): TasteLevel? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromTaste(value: TasteLevel?): String? = value?.name

    @TypeConverter
    fun toOverallReview(raw: String?): OverallReview? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromOverallReview(value: OverallReview?): String? = value?.name
}
