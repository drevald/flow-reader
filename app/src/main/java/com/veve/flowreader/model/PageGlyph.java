package com.veve.flowreader.model;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface PageGlyph {

    /**
     * Draws glyph on page and updates context to place  next glyph
     * @param context sets drawing content
     * @param show is real drawing needed
     */
    void draw(DevicePageContext context, boolean show);
}
