package com.veve.flowreader.model.impl.pdf;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

public class PdfBook implements Book {

    private String path;
    private int pageNumber;


    private String name;

    private long bookId;

    public PdfBook(String path){
        this.bookId = openBook(path);
        this.path = path;
        this.name = path;
    }

    static {
        System.loadLibrary("native-lib");
    }

    private native long openBook(String path);
    private native int getNumberOfPages(long bookId);


    @Override
    public void setCurrentPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public int getCurrentPageNumber() {
        return pageNumber;
    }

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

    @Override
    public long getId() {
        return bookId;
    }

    @Override
    public String getPath() {
        return path;
    }


}
