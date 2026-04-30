@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_VERSION_1 = 1
private const val DATABASE_VERSION_2 = 2
private const val DATABASE_VERSION_3 = 3
private const val DATABASE_VERSION_4 = 4
private const val DATABASE_VERSION_5 = 5
private const val DATABASE_VERSION_6 = 6
private const val DATABASE_VERSION_7 = 7
private const val DATABASE_VERSION_8 = 8

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
                migrateReviewsToV3(db)
            }
        }

    val MIGRATION_3_4: Migration =
        object : Migration(DATABASE_VERSION_3, DATABASE_VERSION_4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateReviewsToV4(db)
            }
        }

    val MIGRATION_4_5: Migration =
        object : Migration(DATABASE_VERSION_4, DATABASE_VERSION_5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sakes` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            }
        }

    val MIGRATION_5_6: Migration =
        object : Migration(DATABASE_VERSION_5, DATABASE_VERSION_6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(createSakesV6TableSql())
                db.execSQL(copySakesToV6Sql())
                db.execSQL("DROP TABLE `sakes`")
                db.execSQL("ALTER TABLE `sakes_new` RENAME TO `sakes`")
            }
        }

    val MIGRATION_6_7: Migration =
        object : Migration(DATABASE_VERSION_6, DATABASE_VERSION_7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sakes` ADD COLUMN `city` TEXT")
            }
        }

    val MIGRATION_7_8: Migration =
        object : Migration(DATABASE_VERSION_7, DATABASE_VERSION_8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reviews` ADD COLUMN `otherFreeComment` TEXT")
            }
        }
}

private fun migrateSakes(db: SupportSQLiteDatabase) {
    db.execSQL(createSakesTableSql())
    db.execSQL(copySakesSql())
    db.execSQL("DROP TABLE `sakes`")
    db.execSQL("ALTER TABLE `sakes_new` RENAME TO `sakes`")
}

private fun migrateReviewsToV3(db: SupportSQLiteDatabase) {
    db.execSQL(createReviewsV3TableSql())
    db.execSQL(copyReviewsToV3Sql())
    db.execSQL("DROP TABLE `reviews`")
    db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
}

private fun migrateReviewsToV4(db: SupportSQLiteDatabase) {
    db.execSQL(createReviewsV4TableSql())
    db.execSQL(copyReviewsToV4Sql())
    db.execSQL("DROP TABLE `reviews`")
    db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
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

private fun createReviewsV3TableSql(): String =
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

private fun copyReviewsToV3Sql(): String =
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

private fun createReviewsV4TableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `price` INTEGER,
        `volume` INTEGER,
        `temperature` TEXT,
        `scene` TEXT,
        `dish` TEXT,
        `appearanceSoundness` TEXT NOT NULL,
        `appearanceColor` TEXT,
        `appearanceViscosity` INTEGER,
        `aromaSoundness` TEXT NOT NULL,
        `aromaIntensity` TEXT,
        `aromaExamples` TEXT NOT NULL,
        `aromaMainNote` TEXT,
        `aromaComplexity` TEXT,
        `tasteSoundness` TEXT NOT NULL,
        `tasteAttack` TEXT,
        `tasteTextureRoundness` TEXT,
        `tasteTextureSmoothness` TEXT,
        `tasteMainNote` TEXT,
        `tasteSweetness` TEXT,
        `tasteSourness` TEXT,
        `tasteBitterness` TEXT,
        `tasteUmami` TEXT,
        `tasteInPalateAroma` TEXT NOT NULL,
        `tasteAftertaste` TEXT,
        `tasteComplexity` TEXT,
        `otherIndividuality` TEXT,
        `otherCautions` TEXT,
        `otherOverallReview` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyReviewsToV4Sql(): String =
    """
    INSERT INTO `reviews_new` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `scene`, `dish`,
        `appearanceSoundness`, `appearanceColor`, `appearanceViscosity`,
        `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`, `aromaComplexity`,
        `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`, `tasteTextureSmoothness`, `tasteMainNote`,
        `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`, `tasteInPalateAroma`,
        `tasteAftertaste`, `tasteComplexity`, `otherIndividuality`, `otherCautions`, `otherOverallReview`
    )
    SELECT
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `scene`, `dish`,
        'SOUND', `color`, `viscosity`,
        'SOUND', `intensity`, `scentTop`, NULL, NULL,
        'SOUND', NULL, NULL, NULL, NULL,
        `sweet`, `sour`, `bitter`, `umami`, `scentMouth`,
        `sharp`, NULL, NULL, `comment`, `review`
    FROM `reviews`
    """.trimIndent()

private fun createSakesV6TableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `sakes_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `name` TEXT NOT NULL,
        `grade` TEXT NOT NULL,
        `isPinned` INTEGER NOT NULL,
        `imageUris` TEXT NOT NULL,
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

private fun copySakesToV6Sql(): String =
    """
    INSERT INTO `sakes_new` (
        `id`, `name`, `grade`, `isPinned`, `imageUris`, `gradeOther`, `type`, `typeOther`, `maker`,
        `prefecture`, `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`, `sakeDegree`,
        `acidity`, `amino`, `yeast`, `water`
    )
    SELECT
        `id`, `name`, `grade`, `isPinned`,
        CASE
            WHEN `imageUri` IS NULL OR TRIM(`imageUri`) = '' THEN '[]'
            ELSE '["' || REPLACE(`imageUri`, '"', '""') || '"]'
        END,
        `gradeOther`, `type`, `typeOther`, `maker`, `prefecture`, `alcohol`, `kojiMai`, `kojiPolish`,
        `kakeMai`, `kakePolish`, `sakeDegree`, `acidity`, `amino`, `yeast`, `water`
    FROM `sakes`
    """.trimIndent()
