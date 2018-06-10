package com.veve.flowreader.model;

import android.content.Context;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.BookStorage;
import com.veve.flowreader.dao.sqlite.BookStorageImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.raster.RasterBook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 12.01.2018.
 */

public class BooksCollection {

    private static List<BookRecord> booksList;

    private static BookStorage bookStorage;

    private static BooksCollection bookCollection;

    private BooksCollection() {

    }

    public static BooksCollection getInstance(Context context) {
        if (bookCollection == null) {
            bookCollection = new BooksCollection();
            bookStorage = BookStorageImpl.getInstance(context);
            booksList = bookStorage.getBooksList();
        }
        return bookCollection;
    }

    public List<BookRecord> getBooks() {
        return booksList;
    }

    public void addBook(BookRecord bookRecord) {
        booksList.add(bookRecord);
        bookStorage.addBook(bookRecord);
    }

    public boolean hasBook(File bookFile) {
        for (BookRecord bookRecord : booksList) {
            if (bookRecord.getUrl().equals(bookFile.getAbsolutePath()))
                return true;
        }
        return false;
    }

}
