package com.veve.flowreader.model;

import android.util.Log;

import com.veve.flowreader.dao.BookRecord;

import java.io.File;

/**
 * Designed as a factory of Book objects of all types
 * Created by ddreval on 4/3/2018.
 */

public class BookFactory {

    private static final BookFactory ourInstance = new BookFactory();

    public static BookFactory getInstance() {
        return ourInstance;
    }

    private BookFactory() {

    }

    public BookSource getSource(File file) {
        return null;
    }

    public BookRecord createBook(File file) {
        BookSource bookSource = getSource(file);
        BookRecord bookRecord = new BookRecord(file.getAbsolutePath(), file.getAbsolutePath());
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ie) {
            Log.e(getClass().getName(), ie.getLocalizedMessage());
        }
        bookRecord.setPagesCount(bookSource.getPagesCount());
        bookRecord.setName(file.getAbsolutePath());
        bookRecord.setCurrentPage(0);
        Log.v(getClass().getName(), String.format("Book created %s", bookSource.toString()));
        return bookRecord;
    }

}
