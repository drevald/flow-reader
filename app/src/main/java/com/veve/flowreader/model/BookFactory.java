package com.veve.flowreader.model;

import com.veve.flowreader.model.impl.djvu.DjvuBook;

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

    public Book createBook(File file) {
        if (file.getName().endsWith("djvu")) {
            return new DjvuBook(file.getAbsolutePath());
        } else {
            return null;
        }
    }

}
