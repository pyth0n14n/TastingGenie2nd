package io.github.pyth0n14n.tastinggenie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.AromaListConverter
import io.github.pyth0n14n.tastinggenie.data.local.converter.FlavorProfileTypeListConverter
import io.github.pyth0n14n.tastinggenie.data.local.converter.ReviewPairingConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.ReviewScalarConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.ReviewTemperatureColorConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.ReviewTextureTasteConverters
import io.github.pyth0n14n.tastinggenie.data.local.converter.SakeConverters
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewModeDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeFoodReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeFoodReviewEntity

@Database(
    entities = [
        SakeEntity::class,
        ReviewEntity::class,
        SakeFoodReviewEntity::class,
        ReviewModeEntity::class,
        ReviewModeItemEntity::class,
    ],
    version = 13,
    exportSchema = true,
)
@TypeConverters(
    SakeConverters::class,
    ReviewTemperatureColorConverters::class,
    ReviewScalarConverters::class,
    ReviewPairingConverters::class,
    ReviewTextureTasteConverters::class,
    AromaListConverter::class,
    FlavorProfileTypeListConverter::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sakeDao(): SakeDao

    abstract fun reviewDao(): ReviewDao

    abstract fun sakeFoodReviewDao(): SakeFoodReviewDao

    abstract fun reviewModeDao(): ReviewModeDao
}
