package com.veve.flowreader.model.impl.raster;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.PageSource;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.pdf.PdfBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 19.04.2018.
 */

public class RasterBook implements Book {

    private Book sourceBook;

    private String path;

    private List<BookPage> pages;

    public RasterBook(BookRecord bookRecord) {
        pages = new ArrayList<BookPage>();
        path = bookRecord.getUrl();
        if (path.toLowerCase().endsWith("djvu")) {
            sourceBook = new DjvuBook(bookRecord.getUrl());
        } else {
            sourceBook = new PdfBook(bookRecord.getUrl());
        }

    }

    @Override
    public void setCurrentPageNumber(int pageNumber) {

    }

    @Override
    public int getCurrentPageNumber() {
        return 0;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        return new RasterBookPage((PageSource)sourceBook.getPage(pageNumber));
    }

    @Override
    public int getPagesCount() {
        return sourceBook.getPagesCount();
    }

    @Override
    public String getName() {
        return sourceBook.getName();
    }

    @Override
    public long getId() {
        return sourceBook.getId();
    }

    @Override
    public String getPath() {
        return path;
    }

}
