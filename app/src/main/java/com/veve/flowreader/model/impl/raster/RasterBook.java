package com.veve.flowreader.model.impl.raster;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.impl.djvu.DjvuBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 19.04.2018.
 */

public class RasterBook implements Book {

    private DjvuBook djvuBook;

    private String path;

    private List<BookPage> pages;

    public RasterBook(BookRecord bookRecord) {
        pages = new ArrayList<BookPage>();
        path = bookRecord.getUrl();
        djvuBook = new DjvuBook(bookRecord.getUrl());
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
//        BookPage page = null;
//        if (pages.isEmpty() || pages.get(pageNumber) == null) {
//            page = new RasterBookPage(djvuBook.getPage(pageNumber));
//            pages.add(pageNumber, page);
//        }
        return new RasterBookPage(djvuBook.getPage(pageNumber));
    }

    @Override
    public int getPagesCount() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getPath() {
        return path;
    }

}
