package com.veve.flowreader.model;

/**
 * Created by ddreval on 12.01.2018.
 */

public interface Book {

    /**
     * Sets curren number (original numbering) of the page.
     * @param pageNumber
     */
    public void setCurrentPageNumber(int pageNumber);

    /**
     * Returns the current number (original numbering) of the page.
     */
    public int getCurrentPageNumber();

    public BookPage getPage(int pageNumber);

    public int getPagesCount();

    public String getName();

    public long getId();

    public String getPath();

}
