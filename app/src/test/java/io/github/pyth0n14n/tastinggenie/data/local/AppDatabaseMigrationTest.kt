package io.github.pyth0n14n.tastinggenie.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MIGRATED_REVIEW_ID = 10L
private const val MIGRATED_REVIEW_EPOCH_DAY = 20_000L
private const val LEGACY_REVIEW_VISCOSITY = 4
private const val LEGACY_DATABASE_VERSION_3 = 3
private const val VERSION_3_IDENTITY_HASH = "da56d247c1fa344031cd57474b9c205b"
private const val LEGACY_DATABASE_VERSION_4 = 4
private const val VERSION_4_IDENTITY_HASH = "27982588b87f31977215b1657c8d2594"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationTest {
    @Test
    fun migration_1_6_preservesExistingSakeData() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-test.db"
            context.deleteDatabase(databaseName)

            createVersion1Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(
                AppDatabaseMigrations.MIGRATION_1_2,
                AppDatabaseMigrations.MIGRATION_2_3,
                AppDatabaseMigrations.MIGRATION_3_4,
                AppDatabaseMigrations.MIGRATION_4_5,
                AppDatabaseMigrations.MIGRATION_5_6,
            )
            val database = databaseBuilder.build()

            val migrated = requireNotNull(database.sakeDao().getById(1L))
            assertEquals("移行前の酒", migrated.name)
            assertEquals("分類その他", migrated.typeOther)
            assertNull(migrated.gradeOther)
            assertEquals(emptyList<String>(), migrated.imageUris)
            assertEquals(false, migrated.isPinned)

            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration_2_6_movesImageColumnToSakesAndDropsItFromReviews() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-2-3-test.db"
            context.deleteDatabase(databaseName)

            createVersion2Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(
                AppDatabaseMigrations.MIGRATION_2_3,
                AppDatabaseMigrations.MIGRATION_3_4,
                AppDatabaseMigrations.MIGRATION_4_5,
                AppDatabaseMigrations.MIGRATION_5_6,
            )
            val database = databaseBuilder.build()

            val migratedSake = requireNotNull(database.sakeDao().getById(1L))
            val migratedReview = requireNotNull(database.reviewDao().getById(MIGRATED_REVIEW_ID))
            val reviewColumns = database.reviewColumnNames()

            assertEquals("移行前の酒", migratedSake.name)
            assertEquals(emptyList<String>(), migratedSake.imageUris)
            assertEquals(false, migratedSake.isPinned)
            assertEquals("移行前レビュー", migratedReview.otherCautions)
            assertEquals(false, reviewColumns.contains("imageUri"))
            assertEquals(false, reviewColumns.contains("comment"))
            assertTrue(reviewColumns.contains("otherCautions"))

            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration_3_6_renamesReviewColumnsAndPreservesReviewValues() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-3-4-test.db"
            context.deleteDatabase(databaseName)

            createVersion3Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(
                AppDatabaseMigrations.MIGRATION_3_4,
                AppDatabaseMigrations.MIGRATION_4_5,
                AppDatabaseMigrations.MIGRATION_5_6,
            )
            val database = databaseBuilder.build()

            val migratedReview = requireNotNull(database.reviewDao().getById(MIGRATED_REVIEW_ID))
            val reviewColumns = database.reviewColumnNames()

            assertEquals("content://bar/1", migratedReview.bar)
            assertEquals("移行前レビュー", migratedReview.otherCautions)
            assertEquals("屋台", migratedReview.scene)
            assertEquals("刺身", migratedReview.dish)
            assertEquals("CLEAR", migratedReview.appearanceColor?.name)
            assertEquals(LEGACY_REVIEW_VISCOSITY, migratedReview.appearanceViscosity)
            assertEquals("MEDIUM", migratedReview.aromaIntensity?.name)
            assertEquals(listOf("MELON"), migratedReview.aromaExamples.map { it.name })
            assertEquals(listOf("PEAR"), migratedReview.tasteInPalateAroma.map { it.name })
            assertEquals("STRONG", migratedReview.tasteSweetness?.name)
            assertEquals("WEAK", migratedReview.tasteAftertaste?.name)
            assertEquals("GOOD", migratedReview.otherOverallReview?.name)
            assertEquals("SOUND", migratedReview.appearanceSoundness.name)
            assertEquals("SOUND", migratedReview.aromaSoundness.name)
            assertEquals("SOUND", migratedReview.tasteSoundness.name)
            assertEquals(false, requireNotNull(database.sakeDao().getById(1L)).isPinned)
            assertEquals(false, reviewColumns.contains("color"))
            assertEquals(false, reviewColumns.contains("comment"))
            assertEquals(false, reviewColumns.contains("scentBase"))
            assertTrue(reviewColumns.contains("appearanceColor"))
            assertTrue(reviewColumns.contains("otherCautions"))

            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration_4_6_addsPinnedColumnWithDefaultFalse() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-4-5-test.db"
            context.deleteDatabase(databaseName)

            createVersion4Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(
                AppDatabaseMigrations.MIGRATION_4_5,
                AppDatabaseMigrations.MIGRATION_5_6,
            )
            val database = databaseBuilder.build()

            val migratedSake = requireNotNull(database.sakeDao().getById(1L))

            assertEquals("移行前の酒", migratedSake.name)
            assertEquals(false, migratedSake.isPinned)

            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun createVersion1Database(
        context: Context,
        databaseName: String,
    ) {
        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(databaseName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(1) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS `sakes` (" +
                                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                        "`name` TEXT NOT NULL, " +
                                        "`grade` TEXT NOT NULL, " +
                                        "`type` TEXT NOT NULL, " +
                                        "`typeOther` TEXT, " +
                                        "`maker` TEXT, " +
                                        "`prefecture` TEXT, " +
                                        "`alcohol` INTEGER, " +
                                        "`kojiMai` TEXT, " +
                                        "`kojiPolish` INTEGER, " +
                                        "`kakeMai` TEXT, " +
                                        "`kakePolish` INTEGER, " +
                                        "`sakeDegree` REAL, " +
                                        "`acidity` REAL, " +
                                        "`amino` REAL, " +
                                        "`yeast` TEXT, " +
                                        "`water` TEXT)",
                                )
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS `reviews` (" +
                                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                        "`sakeId` INTEGER NOT NULL, " +
                                        "`dateEpochDay` INTEGER NOT NULL, " +
                                        "`bar` TEXT, `price` INTEGER, `volume` INTEGER, " +
                                        "`temperature` TEXT, `color` TEXT, `viscosity` INTEGER, " +
                                        "`intensity` TEXT, `scentTop` TEXT NOT NULL, " +
                                        "`scentBase` TEXT NOT NULL, `scentMouth` TEXT NOT NULL, " +
                                        "`sweet` TEXT, `sour` TEXT, `bitter` TEXT, `umami` TEXT, " +
                                        "`sharp` TEXT, `scene` TEXT, `dish` TEXT, `comment` TEXT, " +
                                        "`review` TEXT, `imageUri` TEXT, " +
                                        "FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) " +
                                        "ON UPDATE NO ACTION ON DELETE NO ACTION )",
                                )
                                db.execSQL(
                                    "CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` " +
                                        "ON `reviews` (`sakeId`)",
                                )
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS room_master_table " +
                                        "(id INTEGER PRIMARY KEY,identity_hash TEXT)",
                                )
                                db.execSQL(
                                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
                                        "VALUES(42, '327222c36305fe90defe9a2f46dc5990')",
                                )
                                db.execSQL(
                                    "INSERT INTO `sakes` (" +
                                        "`id`, `name`, `grade`, `type`, `typeOther`, `maker`, `prefecture`, " +
                                        "`alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`, " +
                                        "`sakeDegree`, `acidity`, `amino`, `yeast`, `water`) " +
                                        "VALUES (1, '移行前の酒', 'OTHER', '[]', '分類その他', NULL, NULL, NULL, " +
                                        "NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
                                )
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) = Unit
                        },
                    ).build(),
            )

        helper.writableDatabase.close()
        helper.close()
    }

    private fun createVersion2Database(
        context: Context,
        databaseName: String,
    ) {
        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(databaseName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(2) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS `sakes` (" +
                                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                        "`name` TEXT NOT NULL, `grade` TEXT NOT NULL, `gradeOther` TEXT, " +
                                        "`type` TEXT NOT NULL, `typeOther` TEXT, `maker` TEXT, `prefecture` TEXT, " +
                                        "`alcohol` INTEGER, `kojiMai` TEXT, `kojiPolish` INTEGER, " +
                                        "`kakeMai` TEXT, `kakePolish` INTEGER, `sakeDegree` REAL, `acidity` REAL, " +
                                        "`amino` REAL, `yeast` TEXT, `water` TEXT)",
                                )
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS `reviews` (" +
                                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                        "`sakeId` INTEGER NOT NULL, `dateEpochDay` INTEGER NOT NULL, `bar` TEXT, " +
                                        "`price` INTEGER, `volume` INTEGER, `temperature` TEXT, `color` TEXT, " +
                                        "`viscosity` INTEGER, `intensity` TEXT, `scentTop` TEXT NOT NULL, " +
                                        "`scentBase` TEXT NOT NULL, `scentMouth` TEXT NOT NULL, `sweet` TEXT, " +
                                        "`sour` TEXT, `bitter` TEXT, `umami` TEXT, `sharp` TEXT, `scene` TEXT, " +
                                        "`dish` TEXT, `comment` TEXT, `review` TEXT, `imageUri` TEXT, " +
                                        "FOREIGN KEY(`sakeId`) REFERENCES `sakes`(`id`) " +
                                        "ON UPDATE NO ACTION ON DELETE NO ACTION )",
                                )
                                db.execSQL(
                                    "CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)",
                                )
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS room_master_table " +
                                        "(id INTEGER PRIMARY KEY,identity_hash TEXT)",
                                )
                                db.execSQL(
                                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
                                        "VALUES(42, 'b3b7f4840f90a01ce810e16e80c2a6f9')",
                                )
                                db.execSQL(
                                    """
                                    INSERT INTO `sakes` (
                                        `id`, `name`, `grade`, `gradeOther`, `type`, `typeOther`, `maker`, `prefecture`,
                                        `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`, `sakeDegree`, `acidity`,
                                        `amino`, `yeast`, `water`
                                    ) VALUES (
                                        1, '移行前の酒', 'JUNMAI', NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
                                        NULL, NULL, NULL, NULL, NULL, NULL
                                    )
                                    """.trimIndent(),
                                )
                                db.execSQL(
                                    """
                                    INSERT INTO `reviews` (
                                        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `color`,
                                        `viscosity`, `intensity`, `scentTop`, `scentBase`, `scentMouth`, `sweet`, `sour`,
                                        `bitter`, `umami`, `sharp`, `scene`, `dish`, `comment`, `review`, `imageUri`
                                    ) VALUES (
                                        $MIGRATED_REVIEW_ID, 1, $MIGRATED_REVIEW_EPOCH_DAY, NULL, NULL, NULL, NULL, NULL,
                                        NULL, NULL, '[]', '[]', '[]', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
                                        '移行前レビュー', NULL, 'content://review/image/1'
                                    )
                                    """.trimIndent(),
                                )
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) = Unit
                        },
                    ).build(),
            )

        helper.writableDatabase.close()
        helper.close()
    }

    private fun createVersion3Database(
        context: Context,
        databaseName: String,
    ) {
        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(databaseName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(LEGACY_DATABASE_VERSION_3) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                db.execSQL(createVersion3SakesTableSql())
                                db.execSQL(createVersion3ReviewsTableSql())
                                db.execSQL(
                                    "CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)",
                                )
                                db.execSQL(
                                    createRoomMasterTableSql(),
                                )
                                db.execSQL(
                                    version3IdentitySql(),
                                )
                                db.execSQL(insertVersion3SakeSql())
                                db.execSQL(insertVersion3ReviewSql())
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) = Unit
                        },
                    ).build(),
            )

        helper.writableDatabase.close()
        helper.close()
    }

    private fun createVersion4Database(
        context: Context,
        databaseName: String,
    ) {
        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(databaseName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(LEGACY_DATABASE_VERSION_4) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                db.execSQL(createVersion4SakesTableSql())
                                db.execSQL(createVersion4ReviewsTableSql())
                                db.execSQL(
                                    "CREATE INDEX IF NOT EXISTS `index_reviews_sakeId` ON `reviews` (`sakeId`)",
                                )
                                db.execSQL(createRoomMasterTableSql())
                                db.execSQL(version4IdentitySql())
                                db.execSQL(insertVersion4SakeSql())
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) = Unit
                        },
                    ).build(),
            )

        helper.writableDatabase.close()
        helper.close()
    }
}

private fun AppDatabase.reviewColumnNames(): Set<String> {
    val columns = mutableSetOf<String>()
    openHelper.writableDatabase.query("PRAGMA table_info(`reviews`)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            columns += cursor.getString(nameIndex)
        }
    }
    return columns
}

private fun createVersion3SakesTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `sakes` (
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

private fun createVersion3ReviewsTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews` (
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

private fun version3IdentitySql(): String =
    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
        "VALUES(42, '$VERSION_3_IDENTITY_HASH')"

private fun version4IdentitySql(): String =
    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
        "VALUES(42, '$VERSION_4_IDENTITY_HASH')"

private fun createRoomMasterTableSql(): String =
    "CREATE TABLE IF NOT EXISTS room_master_table " +
        "(id INTEGER PRIMARY KEY,identity_hash TEXT)"

private fun insertVersion3SakeSql(): String =
    """
    INSERT INTO `sakes` (
        `id`, `name`, `grade`, `imageUri`, `gradeOther`, `type`, `typeOther`, `maker`,
        `prefecture`, `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`,
        `sakeDegree`, `acidity`, `amino`, `yeast`, `water`
    ) VALUES (
        1, '移行前の酒', 'JUNMAI', NULL, NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL
    )
    """.trimIndent()

private fun createVersion4SakesTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `sakes` (
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

private fun createVersion4ReviewsTableSql(): String =
    """
    CREATE TABLE IF NOT EXISTS `reviews` (
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

private fun insertVersion4SakeSql(): String =
    """
    INSERT INTO `sakes` (
        `id`, `name`, `grade`, `imageUri`, `gradeOther`, `type`, `typeOther`, `maker`,
        `prefecture`, `alcohol`, `kojiMai`, `kojiPolish`, `kakeMai`, `kakePolish`,
        `sakeDegree`, `acidity`, `amino`, `yeast`, `water`
    ) VALUES (
        1, '移行前の酒', 'JUNMAI', NULL, NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL
    )
    """.trimIndent()

private fun insertVersion3ReviewSql(): String =
    """
    INSERT INTO `reviews` (
        `id`, `sakeId`, `dateEpochDay`, `bar`, `price`, `volume`, `temperature`, `color`,
        `viscosity`, `intensity`, `scentTop`, `scentBase`, `scentMouth`, `sweet`, `sour`,
        `bitter`, `umami`, `sharp`, `scene`, `dish`, `comment`, `review`
    ) VALUES (
        $MIGRATED_REVIEW_ID, 1, $MIGRATED_REVIEW_EPOCH_DAY, 'content://bar/1', 1200, 180, 'JOON',
        'CLEAR', $LEGACY_REVIEW_VISCOSITY, 'MEDIUM', '["MELON"]', '["KONBU"]', '["PEAR"]',
        'STRONG', 'WEAK', 'MEDIUM', 'VERY_STRONG', 'WEAK', '屋台', '刺身', '移行前レビュー', 'GOOD'
    )
    """.trimIndent()
