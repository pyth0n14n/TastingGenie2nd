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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationTest {
    @Test
    fun migration_1_2_addsGradeOtherWithoutDroppingExistingSake() {
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val databaseName = "migration-test.db"
            context.deleteDatabase(databaseName)

            createVersion1Database(context = context, databaseName = databaseName)

            val database =
                Room
                    .databaseBuilder(context, AppDatabase::class.java, databaseName)
                    .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
                    .build()

            val migrated = requireNotNull(database.sakeDao().getById(1L))
            assertEquals("移行前の酒", migrated.name)
            assertEquals("分類その他", migrated.typeOther)
            assertNull(migrated.gradeOther)

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
}
