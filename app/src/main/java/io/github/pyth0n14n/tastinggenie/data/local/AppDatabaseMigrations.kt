package io.github.pyth0n14n.tastinggenie.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_VERSION_1 = 1
private const val DATABASE_VERSION_2 = 2
private const val DATABASE_VERSION_3 = 3

object AppDatabaseMigrations {
    val MIGRATION_1_2: Migration =
        object : Migration(DATABASE_VERSION_1, DATABASE_VERSION_2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sakes ADD COLUMN gradeOther TEXT")
            }
        }

    val MIGRATION_2_3: Migration =
        object : Migration(DATABASE_VERSION_2, DATABASE_VERSION_3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateSakes(db)
                migrateReviews(db)
            }

            private fun migrateSakes(db: SupportSQLiteDatabase) {
                db.execSQL(createSakesTableSql())
                db.execSQL(copySakesSql())
                db.execSQL("DROP TABLE `sakes`")
                db.execSQL("ALTER TABLE `sakes_new` RENAME TO `sakes`")
            }

            private fun migrateReviews(db: SupportSQLiteDatabase) {
                db.execSQL(createReviewsTableSql())
                db.execSQL(copyReviewsSql())
                db.execSQL("DROP TABLE `reviews`")
                db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
            }
        }
}

private fun createSakesTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `sakes_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `name` TEXT NOT NULL,
        `grade` TEXT NOT NULL,
        `imageUri` TEXT,
        `gradeOther` TEXT,
        `type` TEXT NOT NULL,
        `typeOther` TEXT,
        `maker` TEXT,
        `prefecture` TEXT,
        `alcohol` INTEGER,
        `kojiMai` TEXT,
        `kojiPolish` INTEGER,
        `kakeMai` TEXT,
        `kakePolish` INTEGER,
        `sakeDegree` REAL,
        `acidity` REAL,
        `amino` REAL,
        `yeast` TEXT,
        `water` TEXT
    )
    """.trimIndent()

private fun copySakesSql(): String =
    """
    INSERT INTO `sakes_new` (
        `id`, `name`, `grade`, `imageUri`, `gradeOther`, `type`, `typeOther`, `maker`, `prefecture`,
        `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`, `sakeDegree`, `acidity`,
        `amino`, `yeast`, `water`
    )
    SELECT
        `id`, `name`, `grade`, NULL, `gradeOther`, `type`, `typeOther`, `maker`, `prefecture`,
        `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`, `sakeDegree`, `acidity`,
        `amino`, `yeast`, `water`
    FROM `sakes`
    """.trimIndent()

private fun createReviewsTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `price` INTEGER,
        `volume` INTEGER,
        `temperature` TEXT,
        `color` TEXT,
        `viscosity` INTEGER,
        `intensity` TEXT,
        `scentTop` TEXT NOT NULL,
        `scentBase` TEXT NOT NULL,
        `scentMouth` TEXT NOT NULL,
        `sweet` TEXT,
        `sour` TEXT,
        `bitter` TEXT,
        `umami` TEXT,
        `sharp` TEXT,
        `scene` TEXT,
        `dish` TEXT,
        `comment` TEXT,
        `review` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyReviewsSql(): String =
    """
    INSERT INTO `reviews_new` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `color`, `viscosity`,
        `intensity`, `scentTop`, `scentBase`, `scentMouth`, `sweet`, `sour`, `bitter`, `umami`,
        `sharp`, `scene`, `dish`, `comment`, `review`
    )
    SELECT
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `color`, `viscosity`,
        `intensity`, `scentTop`, `scentBase`, `scentMouth`, `sweet`, `sour`, `bitter`, `umami`,
        `sharp`, `scene`, `dish`, `comment`, `review`
    FROM `reviews`
    """.trimIndent()
