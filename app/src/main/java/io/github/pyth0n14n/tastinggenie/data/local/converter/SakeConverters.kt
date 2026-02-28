package io.github.pyth0n14n.tastinggenie.data.local.converter

import androidx.room.TypeConverter
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SakeConverters {
    @TypeConverter
    fun toGrade(raw: String): SakeGrade = enumValueOf(raw)

    @TypeConverter
    fun fromGrade(grade: SakeGrade): String = grade.name

    @TypeConverter
    fun toPrefecture(raw: String?): Prefecture? = raw?.let { enumValueOf(it) }

    @TypeConverter
    fun fromPrefecture(prefecture: Prefecture?): String? = prefecture?.name

    @TypeConverter
    fun toClassificationList(raw: String?): List<SakeClassification> {
        return if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(raw)
        }
    }

    @TypeConverter
    fun fromClassificationList(value: List<SakeClassification>): String = Json.encodeToString(value)
}
