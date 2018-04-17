package com.veve.flowreader.dao.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.BookStorage;
import com.veve.flowreader.dao.BookStorage.*;
import com.veve.flowreader.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 4/3/2018.
 */

public class BookStorageImpl implements BookStorage {

    private static BookStorage bookStorage;

    private List<BookRecord> books;

    private Context dbcontext;

    private SQLiteDatabase database;

    private BookStorageImpl(Context context) {
        dbcontext = context.getApplicationContext();
        database = new BookStorageHelper(dbcontext)
                .getWritableDatabase();
        books = new ArrayList<BookRecord>();
    }

    @Override
    public List<BookRecord> getBooksList() {
        BookRecordCursorWrapper wrapper = queryBooks(null, null);
        try {
            wrapper.moveToFirst();
            while (!wrapper.isAfterLast()) {
                books.add(wrapper.getBookRecord());
                wrapper.moveToNext();
            }
        } finally
        {
            wrapper.close();
        }
        return books;
    }

    public static BookStorage getInstance(Context context) {
        if (bookStorage == null) {
            bookStorage = new BookStorageImpl(context);
        }
        return bookStorage;
    }

    @Override
    public void addBook(Book book) {
        ContentValues values = new ContentValues();
        values.put(BookStorageSchema.BookTable.Cols.PATH, book.getPath());
        long l = database.insert(BookStorageSchema.BookTable.NAME, null, values);
        Log.i(getClass().getName(), String.format("Inserted row number is %d", l));
    }

    private BookRecordCursorWrapper queryBooks(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(
                BookStorageSchema.BookTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new BookRecordCursorWrapper(cursor);
    }

}
