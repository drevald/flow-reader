package com.veve.flowreader.model.impl.mockraster;

import android.util.Log;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 2/16/2018.
 */

public class MockRasterBook implements Book {

    private BookPage page;

    private int pagesTotal = 600;

    private int currentPage = 0;

    private String name;

    public MockRasterBook(String name) {
        this.name = name;
        this.page = new MockRasterBookPage();
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
        Log.i(getClass().getName(), "Getting page #" + pageNumber);
        return new MockRasterBookPage(pageNumber);
    }

    @Override
    public int getPagesCount() {
        return pagesTotal;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return 0;
    }

}
