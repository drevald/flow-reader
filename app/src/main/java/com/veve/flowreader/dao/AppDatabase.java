package com.veve.flowreader.dao;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.veve.flowreader.R;
import com.veve.flowreader.Utils;
import com.veve.flowreader.model.BookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@androidx.room.Database(entities =
        {BookRecord.class, PageGlyphRecord.class, ReportRecord.class}, version = 1, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "db";

    public abstract DaoAccess daoAccess();

    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            RoomDatabase.Builder<AppDatabase> builder =
                    Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME);
            appDatabase = builder.build();
            InitDatabaseTask initDatabaseTask = new InitDatabaseTask(appDatabase.daoAccess());
            initDatabaseTask.execute(context);
        }
        return appDatabase;
    }


    @Override
    public void clearAllTables() {

    }

    static class InitDatabaseTask extends AsyncTask<Context, Void, Void> {

        DaoAccess daoAccess;

        InitDatabaseTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            Context context = contexts[0];
            try {
                File file = new File(context.getExternalFilesDir(null), "sample.pdf");
                InputStream is = context.getResources().openRawResource(R.raw.sample);
                Utils.copy(is, new FileOutputStream(file));
                BookRecord bookRecord = BookFactory.getInstance().createBook(file);
                if(daoAccess.fetchBookByChecksum(bookRecord.getMd5()) == null)
                    daoAccess.addBook(bookRecord);
            } catch (Exception e) {
                Log.e(AppDatabase.class.getName(), "Failed to add sample book", e);
            }
            return null;
        }

    }

}







