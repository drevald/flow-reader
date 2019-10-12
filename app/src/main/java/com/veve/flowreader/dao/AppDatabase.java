package com.veve.flowreader.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@android.arch.persistence.room.Database(entities =
        {BookRecord.class, PageGlyphRecord.class, ReportRecord.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "database";

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

        }
        return appDatabase;
    }

    @Override
    public void clearAllTables() {

    }

}