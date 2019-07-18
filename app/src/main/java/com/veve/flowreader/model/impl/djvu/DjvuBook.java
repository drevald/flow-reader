package com.veve.flowreader.model.impl.djvu;

import android.util.Log;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuBook implements Book {

    static {
        System.loadLibrary("native-lib");
    }

    private long bookId;

    private String path;

    private String name;

    private int currentPageNumber = 0;

    public DjvuBook(String path) {
       this.bookId = openBook(path);
       this.path = path;
       this.name = path;
    }

    private native long openBook(String path);
    private native String openStringBook(String path);
    private native int getNumberOfPages(long bookId);

    @Override
    public BookPage getPage(int pageNumber) {
        currentPageNumber = pageNumber;
        return new DjvuBookPage(bookId, pageNumber);
    }

    @Override
    public int getPagesCount() {
        int numberOfPages = 1;
        try {
            numberOfPages = getNumberOfPages(bookId);
        } catch (Throwable t) {
            Log.d("ERROR", t.getMessage());
        }
        return numberOfPages;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return bookId;
    }

    @Override
    public String getPath() {
        return path;
    }
}
