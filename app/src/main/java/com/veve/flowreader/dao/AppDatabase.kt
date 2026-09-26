package com.veve.flowreader.dao

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.veve.flowreader.R
import com.veve.flowreader.Utils
import com.veve.flowreader.model.BookFactory
import java.io.File
import java.io.FileOutputStream

@Database(
    entities = [BookRecord::class, PageGlyphRecord::class, ReportRecord::class, Settings::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun daoAccess(): DaoAccess
    override fun clearAllTables() {}

    companion object {
        private const val DATABASE_NAME = "db"

        private var appDatabase: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BookRecord ADD COLUMN justify INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BookRecord ADD COLUMN breakOnSpace INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BookRecord ADD COLUMN medianGlyphBaseHeight REAL NOT NULL DEFAULT 0")
            }
        }
        // averageHeight was incorrectly divided by renderZoom before being stored, producing
        // medianGlyphBaseHeight values near 2 px instead of the correct ~48 px. Reset all
        // calibration data so each book recalibrates from freshly extracted glyphs on next open.
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE BookRecord SET medianGlyphBaseHeight = 0")
                database.execSQL("DELETE FROM PageGlyphRecord")
            }
        }

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                InitDatabaseTask(appDatabase!!.daoAccess()).execute(context)
            }
            return appDatabase!!
        }
    }

    private class InitDatabaseTask(private val daoAccess: DaoAccess) : AsyncTask<Context, Void, Void>() {
        override fun doInBackground(vararg contexts: Context): Void? {
            val context = contexts[0]
            try {
                val file = File(context.getExternalFilesDir(null), "sample.pdf")
                val input = context.resources.openRawResource(R.raw.sample)
                Utils.copy(input, FileOutputStream(file))
                val bookRecord = BookFactory.getInstance().createBook(file)
                if (daoAccess.fetchBookByChecksum(bookRecord.md5!!) == null)
                    daoAccess.addBook(bookRecord)
            } catch (e: Exception) {
                Log.e(AppDatabase::class.java.name, "Failed to add sample book", e)
            }
            return null
        }
    }
}
