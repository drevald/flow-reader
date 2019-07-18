package com.veve.flowreader.model;

import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.pdf.PdfBook;

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

    public BookRecord createBook(File file) {
        BookRecord bookRecord = new BookRecord();
        if (file.getName().toLowerCase().endsWith("djvu")) {
            Book book = new DjvuBook(file.getPath());
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getLocalizedMessage());
            }
            bookRecord.setPagesCount(book.getPagesCount());
            bookRecord.setName(book.getName());
        } else if (file.getName().toLowerCase().endsWith("pdf")) {
            Book book = new PdfBook(file.getPath());
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getLocalizedMessage());
            }
            bookRecord.setPagesCount(book.getPagesCount());
            bookRecord.setName(book.getName());
        }
        return bookRecord;
    }

}
