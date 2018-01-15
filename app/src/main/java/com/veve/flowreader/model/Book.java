package com.veve.flowreader.model;

/**
 * Created by ddreval on 12.01.2018.
 */

public interface Book {

    public void setCurrentPageNumber(int pageNumber);

    public int getCurrentPageNumber();

    public BookPage getPage(int pageNumber);

    public int getPagesCount();

    public String getName();

    public long getId();

}
