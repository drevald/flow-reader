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

    private int currentPageNumber = 0;

    private boolean preprocessing;

    public DjvuBook(String path) throws Exception {
        long id = openBook(path);
        if (id < 0) throw new Exception(errorMessage((int) -id));
        this.bookId = id;
        this.path = path;
        this.name = path;
    }

    private static String errorMessage(int code) {
        switch (code) {
            case 1: return "File not found or cannot be opened";
            case 2: return "No permission to read this file";
            case 3: return "Invalid or corrupted DjVu file";
            default: return "Failed to open DjVu file (error " + code + ")";
        }
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
        return getNumberOfPages(bookId);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return getNativeTitle(bookId);
    }

    @Override
    public String getAuthor() {
        return getNativeAuthor(bookId);
    }

    @Override
    public boolean getPreprocessing() {
        return preprocessing;
    }

    @Override
    public void setPreprocessing(boolean preprocessing) {
        this.preprocessing = preprocessing;
    }

    @Override
    public void close() {
        close(bookId);
    }

    @Override
    public long getId() {
        return bookId;
    }

    @Override
    public String getPath() {
        return path;
    }

    private static native String getNativeTitle(long bookId);

    private static native String getNativeAuthor(long bookId);

    private static native int close(long bookId);

}
