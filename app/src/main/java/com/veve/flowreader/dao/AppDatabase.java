package com.veve.flowreader.dao;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

@android.arch.persistence.room.Database(entities =
        {BookRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "db1";

    public abstract DaoAccess daoAccess();

    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
        }
        return appDatabase;
    }

//    @NonNull
//    @Override
//    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
//        return null;
//    }
//
//    @Override
//    protected InvalidationTracker createInvalidationTracker() {
//        return null;
//    }

    @Override
    public void clearAllTables() {

    }

}