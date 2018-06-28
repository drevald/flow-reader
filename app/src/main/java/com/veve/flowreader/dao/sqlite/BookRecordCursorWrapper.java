package com.veve.flowreader.dao.sqlite;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.veve.flowreader.dao.BookRecord;

/**
 * Created by ddreval on 4/4/2018.
 */
public class BookRecordCursorWrapper extends CursorWrapper {

    public BookRecordCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public BookRecord getBookRecord() {
        BookRecord bookRecord = new BookRecord();
        bookRecord.setName(getString(getColumnIndex(BookStorageSchema.BookTable.Cols.NAME)));
        bookRecord.setUrl(getString(getColumnIndex(BookStorageSchema.BookTable.Cols.PATH)));
        bookRecord.setPagesCount(getInt(getColumnIndex(BookStorageSchema.BookTable.Cols.PAGES_COUNT)));
        bookRecord.setCurrentPage(getInt(getColumnIndex(BookStorageSchema.BookTable.Cols.CURRENT_PAGE)));
        bookRecord.setId(getInt(getColumnIndex(BookStorageSchema.BookTable.Cols.ID)));
        return bookRecord;
    }

}