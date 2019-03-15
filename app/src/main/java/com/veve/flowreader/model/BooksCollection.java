package com.veve.flowreader.model;

import android.content.Context;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.BookStorage;

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
            booksList = new ArrayList<BookRecord>();
        }
        return bookCollection;
    }

    public List<BookRecord> getBooks() {
        return booksList;
    }

    public void addBook(BookRecord bookRecord) {
        for (BookRecord record : getBooks()) {
            Log.v(getClass().getName(),
                    "Before addition of id " + bookRecord.getId() + ""+ record.toString());
        }
        int id = bookStorage.addBook(bookRecord);
        bookRecord.setId(id);
        booksList.add(bookRecord);
        for (BookRecord record : getBooks()) {
            Log.v(getClass().getName(),
                    "After addition of id " + bookRecord.getId() + ""+ record.toString());
        }
    }

    public boolean hasBook(File bookFile) {
        for (BookRecord bookRecord : booksList) {
            if (bookRecord.getUrl().equals(bookFile.getAbsolutePath()))
                return true;
        }
        return false;
    }

    public void deleteBook (int bookId) {
        for (BookRecord record : getBooks()) {
            Log.v(getClass().getName(),
                    "Before deletion of id " + bookId + ""+ record.toString());
        }
        bookStorage.deleteBook(bookId);
        BookRecord bookToDelete = null;
        for (BookRecord bookRecord : booksList) {
            if (bookRecord.getId() == bookId) {
                bookToDelete = bookRecord;
                break;
            }
        }
        booksList.remove(bookToDelete);
        for (BookRecord record : getBooks()) {
            Log.v(getClass().getName(),
                    "After deletion of id " + bookId + ""+ record.toString());
        }
    }

}
