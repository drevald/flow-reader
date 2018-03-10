package com.veve.flowreader.model;

import android.graphics.Bitmap;

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

    /**
     * Returns raster representation of page for the given context
     * @param context
     * @return
     */
    public Bitmap getAsBitmap(DevicePageContext context);

    public int getWidth();

    public int getHeight();

}
