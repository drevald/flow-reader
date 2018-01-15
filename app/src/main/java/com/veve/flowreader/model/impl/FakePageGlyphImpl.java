package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.veve.flowreader.model.PageGlyph;

/**
 * Created by ddreval on 15.01.2018.
 */

public class FakePageGlyphImpl implements PageGlyph {

    Paint paint = new Paint();

    @Override
    public Point draw(Point startPoint, Canvas canvas, float zoom) {
        Rect rect = new Rect();
        paint.getTextBounds("A", 0, 1, rect);
        if(rect.width() + startPoint.x > canvas.getWidth()) {
            startPoint.set(0, startPoint.y + rect.height());
        }
        canvas.drawText("A", startPoint.x, startPoint.y, paint);
        Point endPoint = new Point(startPoint.x + rect.width(), startPoint.y);
        return endPoint;
    }

}
