package com.veve.flowreader.model.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import java.util.Random;

/**
 * Created by ddreval on 15.01.2018.
 */

public class MockPageGlyphImpl implements PageGlyph {

    Paint paint;
    String str;

    public MockPageGlyphImpl(String aStr) {
        str = aStr;
        paint = new Paint();
        paint.setTextSize(48);
        paint.setFakeBoldText(true);
    }

    @Override
    public void draw(DevicePageContext context) {
        Point startPoint = context.getStartPoint();
        Canvas canvas = context.getCanvas();
        if(getWidth(context) + startPoint.x > canvas.getWidth()) {
            startPoint.set(0, startPoint.y + getHeight(context));
        }
        canvas.drawText(str, startPoint.x, startPoint.y, paint);
        Point endPoint = new Point(startPoint.x + getWidth(context), startPoint.y);
        context.setStartPoint(endPoint);
    }

    protected int getWidth(DevicePageContext context) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, 1, rect);
        return (int)(rect.width() * context.getZoom());
    }

    protected int getHeight(DevicePageContext context) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, 1, rect);
        return (int)(rect.height() * context.getZoom());
    }

}
