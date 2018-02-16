package com.veve.flowreader.model.impl.mockraster;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 2/16/2018.
 */

public class MockRasterBook implements Book {

    private List<BookPage> pages;

    private int pagesTotal = 10;

    private int currentPage = 0;

    public MockRasterBook() {
        pages = new ArrayList<BookPage>();
    }

    @Override
    public void setCurrentPageNumber(int pageNumber) {
        this.currentPage = pageNumber;
    }

    @Override
    public int getCurrentPageNumber() {
        return this.currentPage;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        BookPage page = pages.get(pageNumber);
        if (page == null) {
            page = new MockRasterBookPage();
            pages.add(pageNumber, page);
        }
        return page;
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

}
