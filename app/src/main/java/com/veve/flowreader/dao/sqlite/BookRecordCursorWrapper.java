package com.veve.flowreader.dao.sqlite;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;

/**
 * Created by ddreval on 4/4/2018.
 */
public class BookRecordCursorWrapper extends CursorWrapper {
    public BookRecordCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public BookRecord getBookRecord() {
        String pathString = getString(getColumnIndex(BookStorageSchema.BookTable.Cols.PATH));
        return new BookRecord(pathString);
    }
}