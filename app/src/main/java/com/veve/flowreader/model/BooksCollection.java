package com.veve.flowreader.model;

import android.content.Context;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.BookStorage;
import com.veve.flowreader.dao.sqlite.BookStorageImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.mockraster.MockRasterBook;
import com.veve.flowreader.model.impl.mocksimple.MockBook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 12.01.2018.
 */

public class BooksCollection {

    private static List<Book> booksList;

    private static BookStorage bookStorage;

    private static BooksCollection bookCollection;

    private BooksCollection() {
        booksList = new ArrayList<Book>();
    }

    public static BooksCollection getInstance(Context context) {
        if (bookCollection == null) {
            bookCollection = new BooksCollection();
            bookStorage = BookStorageImpl.getInstance(context);
            List<BookRecord> bookRecords = bookStorage.getBooksList();
            for (BookRecord bookRecord : bookRecords) {
                Book storedBook = new DjvuBook(bookRecord.getUrl());
                booksList.add(storedBook);
            }
        }
        return bookCollection;
    }

    public List<Book> getBooks() {
        return booksList;
    }

    public void addBook(Book book) {
        booksList.add(book);
        bookStorage.addBook(book);
    }

    public boolean hasBook(File bookFile) {
        for (Book book : booksList) {
            if (book.getPath().equals(bookFile.getAbsolutePath()))
                return true;
        }
        return false;
    }

}
