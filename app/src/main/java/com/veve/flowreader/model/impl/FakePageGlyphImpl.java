package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.veve.flowreader.model.PageGlyph;

import java.util.Random;

/**
 * Created by ddreval on 15.01.2018.
 */

public class FakePageGlyphImpl implements PageGlyph {

    Paint paint = new Paint();
    static Random RANDOM = new Random();

    public FakePageGlyphImpl() {
        paint.setTextSize(48);
        paint.setFakeBoldText(true);
    }

    @Override
    public Point draw(Point startPoint, Canvas canvas, float zoom) {
        Rect rect = new Rect();
        char[] chars = new char[1];
        chars[0] = (char)(64 + RANDOM.nextInt(36));
        String character = new String(chars);
        paint.getTextBounds("A", 0, 1, rect);
        if(rect.width() + startPoint.x > canvas.getWidth()) {
            startPoint.set(0, startPoint.y + rect.height());
        }
        canvas.drawText(character, startPoint.x, startPoint.y, paint);
        Point endPoint = new Point(startPoint.x + rect.width(), startPoint.y);
        return endPoint;
    }

}
