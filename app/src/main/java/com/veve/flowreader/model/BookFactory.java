package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBook;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

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
        BookSource bookSource = null;
        if(file.getPath().endsWith("djvu")) {
            bookSource = new DjvuBookSource(file.getPath());
        } else if (file.getPath().endsWith("pdf")) {
            bookSource = new PdfBookSource(file.getPath());
        }
        return bookSource;
    }

    public BookRecord createBook(File file) {
        BookRecord bookRecord = new BookRecord(file.getAbsolutePath(), file.getAbsolutePath());
        bookRecord.setPagesCount(10);
        bookRecord.setName(file.getAbsolutePath());
        return bookRecord;
    }

}
