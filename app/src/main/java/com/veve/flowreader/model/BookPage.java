package com.veve.flowreader.model;

import android.graphics.Bitmap;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface BookPage {

    /**
     * Returns raster representation of reflowed page for the given context
     * @param context rendering context like page size, zoom, leading, kerning
     * @return rendered bitmap
     */
    Bitmap getAsBitmap(DevicePageContext context);

    Bitmap getAsGrayscaleBitmap(DevicePageContext context);

    int getWidth();

    int getHeight();



}
