package com.veve.flowreader.dao;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

@android.arch.persistence.room.Database(entities = {
BookRecord.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    protected static final String DATABASE_NAME = "db";

    public abstract DaoAccess daoAccess();

    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            boolean databasePresent = (dbFile != null && dbFile.exists());
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
            if (!databasePresent) {
                DaoAccess daoAccess = appDatabase.daoAccess();

            }
        }
        return appDatabase;
    }


    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }

}