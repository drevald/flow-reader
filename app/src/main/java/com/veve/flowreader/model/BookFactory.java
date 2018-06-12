package com.veve.flowreader.model;

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
        BookRecord bookRecord = new BookRecord(file.getAbsolutePath(), file.getAbsolutePath());
        bookRecord.setPagesCount(10);
        bookRecord.setName(file.getAbsolutePath());
        return bookRecord;
    }

}
