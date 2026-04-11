package io.github.pyth0n14n.tastinggenie.data.local.converter

import androidx.room.TypeConverter
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

class ReviewTemperatureColorConverters {
    @TypeConverter
    fun toTemperature(raw: String?): Temperature? = raw?.let { enumValueOf<Temperature>(it) }

    @TypeConverter
    fun fromTemperature(value: Temperature?): String? = value?.name

    @TypeConverter
    fun toColor(raw: String?): SakeColor? = raw?.let { enumValueOf<SakeColor>(it) }

    @TypeConverter
    fun fromColor(value: SakeColor?): String? = value?.name
}

class ReviewScalarConverters {
    @TypeConverter
    fun toSoundness(raw: String?): ReviewSoundness? = raw?.let { enumValueOf<ReviewSoundness>(it) }

    @TypeConverter
    fun fromSoundness(value: ReviewSoundness?): String? = value?.name

    @TypeConverter
    fun toIntensity(raw: String?): IntensityLevel? = raw?.let { enumValueOf<IntensityLevel>(it) }

    @TypeConverter
    fun fromIntensity(value: IntensityLevel?): String? = value?.name

    @TypeConverter
    fun toComplexity(raw: String?): ComplexityLevel? = raw?.let { enumValueOf<ComplexityLevel>(it) }

    @TypeConverter
    fun fromComplexity(value: ComplexityLevel?): String? = value?.name

    @TypeConverter
    fun toAttack(raw: String?): AttackLevel? = raw?.let { enumValueOf<AttackLevel>(it) }

    @TypeConverter
    fun fromAttack(value: AttackLevel?): String? = value?.name
}

class ReviewTextureTasteConverters {
    @TypeConverter
    fun toTextureRoundness(raw: String?): TextureRoundness? = raw?.let { enumValueOf<TextureRoundness>(it) }

    @TypeConverter
    fun fromTextureRoundness(value: TextureRoundness?): String? = value?.name

    @TypeConverter
    fun toTextureSmoothness(raw: String?): TextureSmoothness? = raw?.let { enumValueOf<TextureSmoothness>(it) }

    @TypeConverter
    fun fromTextureSmoothness(value: TextureSmoothness?): String? = value?.name

    @TypeConverter
    fun toTaste(raw: String?): TasteLevel? = raw?.let { enumValueOf<TasteLevel>(it) }

    @TypeConverter
    fun fromTaste(value: TasteLevel?): String? = value?.name

    @TypeConverter
    fun toOverallReview(raw: String?): OverallReview? = raw?.let { enumValueOf<OverallReview>(it) }

    @TypeConverter
    fun fromOverallReview(value: OverallReview?): String? = value?.name
}
