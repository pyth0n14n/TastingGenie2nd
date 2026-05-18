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
private const val DATABASE_VERSION_9 = 9
private const val DATABASE_VERSION_10 = 10
private const val DATABASE_VERSION_11 = 11
private const val DATABASE_VERSION_12 = 12
private const val DATABASE_VERSION_13 = 13

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

    val MIGRATION_8_9: Migration =
        object : Migration(DATABASE_VERSION_8, DATABASE_VERSION_9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reviews` ADD COLUMN `appearanceColorOther` TEXT")
            }
        }

    val MIGRATION_9_10: Migration =
        object : Migration(DATABASE_VERSION_9, DATABASE_VERSION_10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateReviewsToV10(db)
                createReviewModeTables(db)
                seedBuiltInReviewModes(db)
            }
        }

    val MIGRATION_10_11: Migration =
        object : Migration(DATABASE_VERSION_10, DATABASE_VERSION_11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateSakesToV11(db)
            }
        }

    val MIGRATION_11_12: Migration =
        object : Migration(DATABASE_VERSION_11, DATABASE_VERSION_12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateReviewsToV12(db)
            }
        }

    val MIGRATION_12_13: Migration =
        object : Migration(DATABASE_VERSION_12, DATABASE_VERSION_13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createSakeFoodReviewsTable(db)
                migrateFoodReviewsToV13(db)
                migrateReviewsToV13(db)
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

private fun migrateReviewsToV10(db: SupportSQLiteDatabase) {
    db.execSQL(createReviewsV10TableSql())
    db.execSQL(copyReviewsToV10Sql())
    db.execSQL("DROP TABLE `reviews`")
    db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
}

private fun migrateSakesToV11(db: SupportSQLiteDatabase) {
    db.execSQL(createSakesV11TableSql())
    db.execSQL(copySakesToV11Sql())
    db.execSQL("DROP TABLE `sakes`")
    db.execSQL("ALTER TABLE `sakes_new` RENAME TO `sakes`")
}

private fun migrateReviewsToV12(db: SupportSQLiteDatabase) {
    db.execSQL(createReviewsV12TableSql())
    db.execSQL(copyReviewsToV12Sql())
    db.execSQL("DROP TABLE `reviews`")
    db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
}

private fun createSakeFoodReviewsTable(db: SupportSQLiteDatabase) {
    db.execSQL(createSakeFoodReviewsTableSql())
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_sake_food_reviews_sakeId` ON `sake_food_reviews` (`sakeId`)")
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_sake_food_reviews_dateEpochDay` " +
            "ON `sake_food_reviews` (`dateEpochDay`)",
    )
}

private fun migrateFoodReviewsToV13(db: SupportSQLiteDatabase) {
    db.execSQL(copyFoodReviewsToV13Sql())
}

private fun migrateReviewsToV13(db: SupportSQLiteDatabase) {
    db.execSQL(createReviewsV13TableSql())
    db.execSQL(copyReviewsToV13Sql())
    db.execSQL("DROP TABLE `reviews`")
    db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)")
}

private fun createReviewModeTables(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `review_modes` (
            `id` TEXT NOT NULL,
            `label` TEXT NOT NULL,
            `isBuiltIn` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `review_mode_items` (
            `modeId` TEXT NOT NULL,
            `itemId` TEXT NOT NULL,
            `isEnabled` INTEGER NOT NULL,
            PRIMARY KEY(`modeId`, `itemId`),
            FOREIGN KEY(`modeId`) REFERENCES `review_modes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_mode_items_modeId` ON `review_mode_items` (`modeId`)")
}

private fun seedBuiltInReviewModes(db: SupportSQLiteDatabase) {
    db.execSQL("INSERT OR REPLACE INTO `review_modes` (`id`, `label`, `isBuiltIn`) VALUES ('normal', '通常', 1)")
    db.execSQL(
        "INSERT OR REPLACE INTO `review_modes` (`id`, `label`, `isBuiltIn`) VALUES ('kikisake_shi', '利酒師', 1)",
    )
    db.execSQL("INSERT OR REPLACE INTO `review_modes` (`id`, `label`, `isBuiltIn`) VALUES ('debug', 'デバッグ', 1)")
    normalReviewModeItems().forEach { itemId -> insertReviewModeItem(db, modeId = "normal", itemId = itemId) }
    kikisakeShiReviewModeItems().forEach { itemId ->
        insertReviewModeItem(db, modeId = "kikisake_shi", itemId = itemId)
    }
    debugReviewModeItems().forEach { itemId -> insertReviewModeItem(db, modeId = "debug", itemId = itemId) }
}

private fun insertReviewModeItem(
    db: SupportSQLiteDatabase,
    modeId: String,
    itemId: String,
) {
    db.execSQL(
        "INSERT OR REPLACE INTO `review_mode_items` (`modeId`, `itemId`, `isEnabled`) VALUES ('$modeId', '$itemId', 1)",
    )
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

private fun createReviewsV10TableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `price` INTEGER,
        `volume` INTEGER,
        `temperature` TEXT,
        `dish` TEXT,
        `foodCompatibility` TEXT,
        `appearanceSoundness` TEXT NOT NULL,
        `appearanceColor` TEXT,
        `appearanceColorOther` TEXT,
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
        `tasteTextureNote` TEXT,
        `tasteSweetness` TEXT,
        `tasteSourness` TEXT,
        `tasteBitterness` TEXT,
        `tasteUmami` TEXT,
        `tasteDescription` TEXT,
        `tasteSweetDryness` TEXT,
        `tasteInPalateAromaIntensity` TEXT,
        `tasteInPalateAroma` TEXT NOT NULL,
        `tasteAftertaste` TEXT,
        `tasteAftertasteNote` TEXT,
        `tasteComplexity` TEXT,
        `otherIndividuality` TEXT,
        `otherCautions` TEXT,
        `otherSakeTypes` TEXT NOT NULL,
        `otherFreeComment` TEXT,
        `otherOverallReview` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyReviewsToV10Sql(): String =
    """
    INSERT INTO `reviews_new` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `dish`,
        `appearanceSoundness`, `aromaSoundness`, `tasteSoundness`, `aromaExamples`, `tasteInPalateAroma`,
        `otherSakeTypes`
    )
    SELECT
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `dish`,
        'SOUND', 'SOUND', 'SOUND', '[]', '[]', '[]'
    FROM `reviews`
    """.trimIndent()

private fun createReviewsV12TableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `price` INTEGER,
        `volume` INTEGER,
        `temperature` TEXT,
        `dish` TEXT,
        `foodCompatibility` TEXT,
        `appearanceSoundness` TEXT,
        `appearanceColor` TEXT,
        `appearanceColorOther` TEXT,
        `appearanceViscosity` INTEGER,
        `aromaSoundness` TEXT,
        `aromaIntensity` TEXT,
        `aromaExamples` TEXT NOT NULL,
        `aromaMainNote` TEXT,
        `aromaComplexity` TEXT,
        `tasteSoundness` TEXT,
        `tasteAttack` TEXT,
        `tasteTextureRoundness` TEXT,
        `tasteTextureSmoothness` TEXT,
        `tasteTextureNote` TEXT,
        `tasteSweetness` TEXT,
        `tasteSourness` TEXT,
        `tasteBitterness` TEXT,
        `tasteUmami` TEXT,
        `tasteDescription` TEXT,
        `tasteSweetDryness` TEXT,
        `tasteInPalateAromaIntensity` TEXT,
        `tasteInPalateAroma` TEXT NOT NULL,
        `tasteAftertaste` TEXT,
        `tasteAftertasteNote` TEXT,
        `tasteComplexity` TEXT,
        `otherIndividuality` TEXT,
        `otherCautions` TEXT,
        `otherSakeTypes` TEXT NOT NULL,
        `otherFreeComment` TEXT,
        `otherOverallReview` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyReviewsToV12Sql(): String =
    """
    INSERT INTO `reviews_new` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `dish`,
        `foodCompatibility`, `appearanceSoundness`, `appearanceColor`, `appearanceColorOther`,
        `appearanceViscosity`, `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`,
        `aromaComplexity`, `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`,
        `tasteTextureSmoothness`, `tasteTextureNote`, `tasteSweetness`, `tasteSourness`,
        `tasteBitterness`, `tasteUmami`, `tasteDescription`, `tasteSweetDryness`,
        `tasteInPalateAromaIntensity`, `tasteInPalateAroma`, `tasteAftertaste`, `tasteAftertasteNote`,
        `tasteComplexity`, `otherIndividuality`, `otherCautions`, `otherSakeTypes`,
        `otherFreeComment`, `otherOverallReview`
    )
    SELECT
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `dish`,
        `foodCompatibility`, `appearanceSoundness`, `appearanceColor`, `appearanceColorOther`,
        `appearanceViscosity`, `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`,
        `aromaComplexity`, `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`,
        `tasteTextureSmoothness`, `tasteTextureNote`, `tasteSweetness`, `tasteSourness`,
        `tasteBitterness`, `tasteUmami`, `tasteDescription`, `tasteSweetDryness`,
        `tasteInPalateAromaIntensity`, `tasteInPalateAroma`, `tasteAftertaste`, `tasteAftertasteNote`,
        `tasteComplexity`, `otherIndividuality`, `otherCautions`, `otherSakeTypes`,
        `otherFreeComment`, `otherOverallReview`
    FROM `reviews`
    """.trimIndent()

private fun createSakeFoodReviewsTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `sake_food_reviews` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `dish` TEXT,
        `foodCompatibility` TEXT,
        `temperature` TEXT,
        `freeComment` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyFoodReviewsToV13Sql(): String =
    """
    INSERT INTO `sake_food_reviews` (
        `sakeId`, `dateEpochDay`, `bar`, `dish`, `foodCompatibility`, `temperature`, `freeComment`
    )
    SELECT
        `sakeId`, `dateEpochDay`, `bar`, `dish`, `foodCompatibility`, `temperature`, NULL
    FROM `reviews`
    WHERE (`dish` IS NOT NULL AND TRIM(`dish`) != '') OR `foodCompatibility` IS NOT NULL
    """.trimIndent()

private fun createReviewsV13TableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `sakeId` INTEGER NOT NULL,
        `dateEpochDay` INTEGER NOT NULL,
        `bar` TEXT,
        `price` INTEGER,
        `volume` INTEGER,
        `temperature` TEXT,
        `appearanceSoundness` TEXT,
        `appearanceColor` TEXT,
        `appearanceColorOther` TEXT,
        `appearanceViscosity` INTEGER,
        `aromaSoundness` TEXT,
        `aromaIntensity` TEXT,
        `aromaExamples` TEXT NOT NULL,
        `aromaMainNote` TEXT,
        `aromaComplexity` TEXT,
        `tasteSoundness` TEXT,
        `tasteAttack` TEXT,
        `tasteTextureRoundness` TEXT,
        `tasteTextureSmoothness` TEXT,
        `tasteTextureNote` TEXT,
        `tasteSweetness` TEXT,
        `tasteSourness` TEXT,
        `tasteBitterness` TEXT,
        `tasteUmami` TEXT,
        `tasteDescription` TEXT,
        `tasteSweetDryness` TEXT,
        `tasteInPalateAromaIntensity` TEXT,
        `tasteInPalateAroma` TEXT NOT NULL,
        `tasteAftertaste` TEXT,
        `tasteAftertasteNote` TEXT,
        `tasteComplexity` TEXT,
        `otherIndividuality` TEXT,
        `otherCautions` TEXT,
        `otherSakeTypes` TEXT NOT NULL,
        `otherFreeComment` TEXT,
        `otherOverallReview` TEXT,
        FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    """.trimIndent()

private fun copyReviewsToV13Sql(): String =
    """
    INSERT INTO `reviews_new` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`,
        `appearanceSoundness`, `appearanceColor`, `appearanceColorOther`, `appearanceViscosity`,
        `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`, `aromaComplexity`,
        `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`, `tasteTextureSmoothness`,
        `tasteTextureNote`, `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`,
        `tasteDescription`, `tasteSweetDryness`, `tasteInPalateAromaIntensity`, `tasteInPalateAroma`,
        `tasteAftertaste`, `tasteAftertasteNote`, `tasteComplexity`, `otherIndividuality`,
        `otherCautions`, `otherSakeTypes`, `otherFreeComment`, `otherOverallReview`
    )
    SELECT
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`,
        `appearanceSoundness`, `appearanceColor`, `appearanceColorOther`, `appearanceViscosity`,
        `aromaSoundness`, `aromaIntensity`, `aromaExamples`, `aromaMainNote`, `aromaComplexity`,
        `tasteSoundness`, `tasteAttack`, `tasteTextureRoundness`, `tasteTextureSmoothness`,
        `tasteTextureNote`, `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`,
        `tasteDescription`, `tasteSweetDryness`, `tasteInPalateAromaIntensity`, `tasteInPalateAroma`,
        `tasteAftertaste`, `tasteAftertasteNote`, `tasteComplexity`, `otherIndividuality`,
        `otherCautions`, `otherSakeTypes`, `otherFreeComment`, `otherOverallReview`
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

private fun createSakesV11TableSql(): String =
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
        `city` TEXT,
        `alcohol` REAL,
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

private fun copySakesToV11Sql(): String =
    """
    INSERT INTO `sakes_new` (
        `id`, `name`, `grade`, `isPinned`, `imageUris`, `gradeOther`, `type`, `typeOther`, `maker`,
        `prefecture`, `city`, `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`,
        `sakeDegree`, `acidity`, `amino`, `yeast`, `water`
    )
    SELECT
        `id`, `name`, `grade`, `isPinned`, `imageUris`, `gradeOther`, `type`, `typeOther`, `maker`,
        `prefecture`, `city`, CAST(`alcohol` AS REAL), `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`,
        `sakeDegree`, `acidity`, `amino`, `yeast`, `water`
    FROM `sakes`
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

private fun normalReviewModeItems(): List<String> =
    listOf(
        "DATE",
        "PRICE",
        "VOLUME",
        "TEMPERATURE",
        "BAR",
        "APPEARANCE_SOUNDNESS",
        "APPEARANCE_COLOR",
        "APPEARANCE_VISCOSITY",
        "AROMA_SOUNDNESS",
        "AROMA_INTENSITY",
        "AROMA_EXAMPLES",
        "AROMA_COMPLEXITY",
        "TASTE_SOUNDNESS",
        "TASTE_ATTACK",
        "TASTE_TEXTURE_ROUNDNESS",
        "TASTE_TEXTURE_SMOOTHNESS",
        "TASTE_SWEETNESS",
        "TASTE_SOURNESS",
        "TASTE_UMAMI",
        "TASTE_BITTERNESS",
        "TASTE_SWEET_DRYNESS",
        "TASTE_IN_PALATE_AROMA_INTENSITY",
        "TASTE_IN_PALATE_AROMA_EXAMPLES",
        "TASTE_AFTERTASTE_LENGTH",
        "TASTE_COMPLEXITY",
        "OTHER_INDIVIDUALITY",
        "OTHER_SAKE_TYPES",
        "OTHER_FREE_COMMENT",
        "OTHER_OVERALL_REVIEW",
    )

private fun kikisakeShiReviewModeItems(): List<String> =
    listOf(
        "DATE",
        "PRICE",
        "VOLUME",
        "TEMPERATURE",
        "BAR",
        "APPEARANCE_SOUNDNESS",
        "APPEARANCE_COLOR",
        "APPEARANCE_VISCOSITY",
        "AROMA_SOUNDNESS",
        "AROMA_INTENSITY",
        "AROMA_EXAMPLES",
        "AROMA_MAIN_NOTE",
        "TASTE_SOUNDNESS",
        "TASTE_ATTACK",
        "TASTE_TEXTURE_NOTE",
        "TASTE_DESCRIPTION",
        "TASTE_SWEET_DRYNESS",
        "TASTE_IN_PALATE_AROMA_INTENSITY",
        "TASTE_IN_PALATE_AROMA_EXAMPLES",
        "TASTE_AFTERTASTE_LENGTH",
        "TASTE_AFTERTASTE_NOTE",
        "TASTE_COMPLEXITY",
        "OTHER_INDIVIDUALITY",
        "OTHER_CAUTIONS",
        "OTHER_SAKE_TYPES",
        "OTHER_FREE_COMMENT",
        "OTHER_OVERALL_REVIEW",
    )

private fun debugReviewModeItems(): List<String> =
    listOf(
        "DATE",
        "PRICE",
        "VOLUME",
        "TEMPERATURE",
        "BAR",
        "APPEARANCE_SOUNDNESS",
        "APPEARANCE_COLOR",
        "APPEARANCE_VISCOSITY",
        "AROMA_SOUNDNESS",
        "AROMA_INTENSITY",
        "AROMA_EXAMPLES",
        "AROMA_MAIN_NOTE",
        "AROMA_COMPLEXITY",
        "TASTE_SOUNDNESS",
        "TASTE_ATTACK",
        "TASTE_TEXTURE_ROUNDNESS",
        "TASTE_TEXTURE_SMOOTHNESS",
        "TASTE_TEXTURE_NOTE",
        "TASTE_SWEETNESS",
        "TASTE_SOURNESS",
        "TASTE_UMAMI",
        "TASTE_BITTERNESS",
        "TASTE_DESCRIPTION",
        "TASTE_SWEET_DRYNESS",
        "TASTE_IN_PALATE_AROMA_INTENSITY",
        "TASTE_IN_PALATE_AROMA_EXAMPLES",
        "TASTE_AFTERTASTE_LENGTH",
        "TASTE_AFTERTASTE_NOTE",
        "TASTE_COMPLEXITY",
        "OTHER_INDIVIDUALITY",
        "OTHER_CAUTIONS",
        "OTHER_SAKE_TYPES",
        "OTHER_FREE_COMMENT",
        "OTHER_OVERALL_REVIEW",
    )
