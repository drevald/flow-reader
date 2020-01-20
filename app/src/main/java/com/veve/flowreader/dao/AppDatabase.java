package com.veve.flowreader.dao;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.veve.flowreader.R;
import com.veve.flowreader.Utils;
import com.veve.flowreader.model.BookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@androidx.room.Database(entities =
        {BookRecord.class, PageGlyphRecord.class, ReportRecord.class}, version = 2, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "database149";

    public abstract DaoAccess daoAccess();

    private static AppDatabase appDatabase;

    static final Migration MIGRATION_1_2= new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            RoomDatabase.Builder<AppDatabase> builder =
                    Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME);
            builder.addMigrations(new Migration(1, 2) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {}
            });
            appDatabase = builder.build();
            try {
                appDatabase.addSampleBook(context);
            } catch (Exception e) {
                Log.e(AppDatabase.class.getName(), "Failed to add sample book", e);
            }

        }
        return appDatabase;
    }

    private void addSampleBook(Context context) throws Exception {
        File file = new File(context.getExternalFilesDir(null), "sample.pdf");
        InputStream is = context.getResources().openRawResource(R.raw.pdf_sample);
        Utils.copy(is, new FileOutputStream(file));
        BookRecord bookRecord = BookFactory.getInstance().createBook(file);
        this.daoAccess().addBook(bookRecord);
    }

    @Override
    public void clearAllTables() {

    }

}