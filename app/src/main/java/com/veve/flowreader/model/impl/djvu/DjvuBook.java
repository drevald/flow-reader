package com.veve.flowreader.model.impl.djvu;

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

    private int currentPageNumber = 1;
    public DjvuBook(String path) {
       this.bookId = openBook(path);
       this.path = path;
       this.name = path;
    }

    private native long openBook(String path);


    @Override
    public void setCurrentPageNumber(int pageNumber) {
        this.currentPageNumber = pageNumber;
    }

    @Override
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        currentPageNumber = pageNumber;
        return new DjvuBookPage(bookId, pageNumber);
    }

    @Override
    public int getPagesCount() {
        return 0;
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
