package com.veve.flowreader.model;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface BookPage {

    /**
     * Getting symbol by symbol from the page until they are over.
     * After that null will be returned.
     * @return
     */
    public PageGlyph getNextGlyph();

    /**
     * Call this method to get to the page start and be able
     * to read from the beginning
     */
    public void reset();

}
