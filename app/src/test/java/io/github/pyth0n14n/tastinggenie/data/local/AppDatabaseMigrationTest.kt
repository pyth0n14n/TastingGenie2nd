package io.github.pyth0n14n.tastinggenie.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MIGRATED_REVIEW_ID = 10L
private const val MIGRATED_REVIEW_EPOCH_DAY = 20_000L

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationTest {
    @Test
    fun migration_1_3_preservesExistingSakeData() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-test.db"
            context.deleteDatabase(databaseName)

            createVersion1Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(
                AppDatabaseMigrations.MIGRATION_1_2,
                AppDatabaseMigrations.MIGRATION_2_3,
            )
            val database = databaseBuilder.build()

            val migrated = requireNotNull(database.sakeDao().getById(1L))
            assertEquals("移行前の酒", migrated.name)
            assertEquals("分類その他", migrated.typeOther)
            assertNull(migrated.gradeOther)
            assertNull(migrated.imageUri)

            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration_2_3_movesImageColumnToSakesAndDropsItFromReviews() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-2-3-test.db"
            context.deleteDatabase(databaseName)

            createVersion2Database(context = context, databaseName = databaseName)

            val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            databaseBuilder.addMigrations(AppDatabaseMigrations.MIGRATION_2_3)
            val database = databaseBuilder.build()

            val migratedSake = requireNotNull(database.sakeDao().getById(1L))
            val migratedReview = requireNotNull(database.reviewDao().getById(MIGRATED_REVIEW_ID))
            val reviewColumns = database.reviewColumnNames()

            assertEquals("移行前の酒", migratedSake.name)
            assertNull(migratedSake.imageUri)
            assertEquals("移行前レビュー", migratedReview.comment)
            assertEquals(false, reviewColumns.contains("imageUri"))

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
