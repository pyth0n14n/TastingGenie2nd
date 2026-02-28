package io.github.pyth0n14n.tastinggenie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.AromaListConverter
import io.github.pyth0n14n.tastinggenie.data.local.converter.ReviewEnumConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.SakeConverters
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity

@Database(
    entities = [SakeEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    SakeConverters::class,
    ReviewEnumConverters::class,
    AromaListConverter::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sakeDao(): SakeDao

    abstract fun reviewDao(): ReviewDao
}
