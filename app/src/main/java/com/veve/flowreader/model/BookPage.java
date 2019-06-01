package com.veve.flowreader.model;

import android.graphics.Bitmap;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface BookPage {

    /**
     * Returns raster representation of reflowed page for the given context
     * @param context
     * @return
     */
    public Bitmap getAsBitmap(DevicePageContext context);

    public int getWidth();

    public int getHeight();

}
