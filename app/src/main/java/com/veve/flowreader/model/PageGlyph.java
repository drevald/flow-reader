package com.veve.flowreader.model;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface PageGlyph {

    public Point draw(Point startPoint, Canvas canvas, float zoom);

}
