package com.veve.flowreader.model;

/**
 * Created by ddreval on 12.01.2018.
 */

public interface Book {

    /**
     * Returns the current number (original numbering) of the page.
     */

    BookPage getPage(int pageNumber);

    int getPagesCount();

    String getName();

    long getId();

    String getPath();

    String getTitle();

    String getAuthor();

}
