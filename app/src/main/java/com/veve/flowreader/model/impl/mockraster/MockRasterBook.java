package com.veve.flowreader.model.impl.mockraster;

import android.util.Log;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

/**
 * Created by ddreval on 2/16/2018.
 */

public class MockRasterBook implements Book {

    private int pagesTotal = 600;

    private int currentPage = 0;

    private String name;

    public MockRasterBook(String name) {
        this.name = name;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        Log.i(getClass().getName(), "Getting page #" + pageNumber);
        return new MockRasterBookPage();
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

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public boolean getPreprocessing() {
        return false;
    }

    @Override
    public void setPreprocessing(boolean preprocessing) {

    }

}
