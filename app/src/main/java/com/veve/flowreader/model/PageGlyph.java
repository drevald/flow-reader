package com.veve.flowreader.model;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface PageGlyph {

    /**
     * Draws glyph on page and updates context to place  next glyph
     * @param context
     */
    public void draw(DevicePageContext context);

    /**
     * Recalculates glyph position without actually draing it
     * @param context
     */
    public void virtualDraw(DevicePageContext context);

}
