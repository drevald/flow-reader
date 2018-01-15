package com.veve.flowreader.model.impl;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

/**
 * Created by ddreval on 12.01.2018.
 */

public class SampleDjvuBook implements Book {

    @Override
    public void setCurrentPageNumber(int pageNumber) {

    }

    @Override
    public int getCurrentPageNumber() {
        return 0;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        return new SamplePageImpl();
    }

    @Override
    public int getPagesCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "Sample DjVu Book";
    }

    @Override
    public long getId() {
        return 0;
    }
}
