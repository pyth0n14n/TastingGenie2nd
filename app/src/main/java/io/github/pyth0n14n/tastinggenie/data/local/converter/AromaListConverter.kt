package io.github.pyth0n14n.tastinggenie.data.local.converter

import androidx.room.TypeConverter
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AromaListConverter {
    @TypeConverter
    fun toAromaList(raw: String?): List<Aroma> =
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(raw)
        }

    @TypeConverter
    fun fromAromaList(values: List<Aroma>): String = Json.encodeToString(values)
}

class FlavorProfileTypeListConverter {
    @TypeConverter
    fun toFlavorProfileTypeList(raw: String?): List<FlavorProfileType> =
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(raw)
        }

    @TypeConverter
    fun fromFlavorProfileTypeList(values: List<FlavorProfileType>): String = Json.encodeToString(values)
}
