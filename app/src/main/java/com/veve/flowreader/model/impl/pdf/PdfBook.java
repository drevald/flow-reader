package com.veve.flowreader.model.impl.pdf;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

public class PdfBook implements Book {

    private String path;

    private int pageNumber;

    private String name;

    private long bookId;

    private boolean preprocessing;

    public PdfBook(String path) throws Exception {
        long id = openBook(path);
        if (id < 0) throw new Exception(errorMessage((int) -id));
        this.bookId = id;
        this.path = path;
        this.name = path;
    }

    private static String errorMessage(int code) {
        switch (code) {
            case 2: return "File not found or cannot be opened";
            case 3: return "Invalid or corrupted PDF file";
            case 4: return "Password-protected PDF is not supported";
            case 5: return "Unsupported PDF security scheme";
            case 6: return "PDF content error";
            default: return "Failed to open PDF (error " + code + ")";
        }
    }

    static {
        System.loadLibrary("native-lib");
    }

    private native long openBook(String path);
    private native int getNumberOfPages(long bookId);
    private native String getNativeTitle(long bookId);
    private native String getNativeAuthor(long bookId);

    @Override
    public BookPage getPage(int pageNumber) {

        return new PdfBookPage(bookId, pageNumber);
    }

    @Override
    public int getPagesCount() {
        return getNumberOfPages(bookId);
    }

    @Override
    public String getName() {
        return path;
    }

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

    private static native int close(long bookId);

}
