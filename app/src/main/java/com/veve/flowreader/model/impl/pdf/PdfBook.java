package com.veve.flowreader.model.impl.pdf;

import com.artifex.mupdf.fitz.Document;
import com.artifex.mupdf.fitz.Page;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

public class PdfBook implements Book {

    private String path;
    private int pageNumber;
    private Document document;

    public PdfBook(String path){
        document = Document.openDocument(path);
        this.path = path;
    }

    static {
        System.loadLibrary("mupdf_java");
    }

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
        Page page = document.loadPage(pageNumber);
        return new PdfBookPage(page);
    }

    @Override
    public int getPagesCount() {
        return document.countPages();
    }

    @Override
    public String getName() {
        return path;
    }

    @Override
    public long getId() {
        return document.getPointer();
    }

    @Override
    public String getPath() {
        return path;
    }

}
