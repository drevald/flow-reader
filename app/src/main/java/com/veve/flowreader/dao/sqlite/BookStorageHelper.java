package com.veve.flowreader.dao.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.veve.flowreader.dao.sqlite.BookStorageSchema.*;

/**
 * Created by ddreval on 4/4/2018.
 */

public class BookStorageHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    private static final String DATABASE_NAME = "BookStorage.db";

    public BookStorageHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + BookTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                BookTable.Cols.PATH +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}