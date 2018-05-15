package com.veve.flowreader.model.impl.mocksimple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    String str;

    public static int defaultFontSize = 48;
    public static Rect rect = new Rect();
    public static Paint paint = new Paint();
    private static Paint paint1 = new Paint();

    public MockPageGlyphImpl(String aStr) {
        str = aStr;
        paint.setTextSize(defaultFontSize);
        paint.setFakeBoldText(true);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setColor(Color.BLUE);
        paint1.setStrokeWidth(1);
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {
        Point startPoint = context.getStartPoint();
        Canvas canvas = context.getCanvas();
        int __height = getHeight(context);
        int __width = getWidth(context);
        if(getWidth(context) + startPoint.x > context.getWidth()) {
            startPoint.set(0, startPoint.y + __height + (int)(context.getLeading()* context.getZoom()));
        }
        paint.setTextSize(defaultFontSize * context.getZoom());

        if(show) {
            Log.d("Glyph", (String.format("Text \"%S\" at x=%d, y=%d", str, startPoint.x, startPoint.y + __height)));
            canvas.drawText(str, startPoint.x, startPoint.y + __height, paint);
        }

        context.getStartPoint().set(startPoint.x + __width
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);

        context.getRemotestPoint().set(startPoint.x + __width
                + (int)(context.getKerning()* context.getZoom()), startPoint.y + __height);
    }

    protected int getWidth(DevicePageContext context) {
        paint.setTextSize(defaultFontSize * context.getZoom());
        paint.getTextBounds(str, 0, 1, rect);
        Log.d("Glyph", String.format("font %S size is %dx%d", str, rect.width(), rect.height()));
        return (int)(rect.width());
    }

    protected int getHeight(DevicePageContext context) {
        paint.setTextSize(defaultFontSize * context.getZoom());
        paint.getTextBounds(str, 0, 1, rect);
        return (int)(rect.height());
    }

}
