package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.Constants;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;

import java.util.List;


/**
 * Created by ddreval on 19.04.2018.
 */

public class PageGlyphImpl implements PageGlyph {

    private static Paint paint = new Paint();

    private static Paint paint_debug = new Paint();

    private Bitmap bitmap;

    private int baseLineShift;

    public int getBaseLineShift() {
        return baseLineShift;
    }

    public int getAverageHeight() {
        return averageHeight;
    }

    public boolean isIndented() {
        return indented;
    }

    private int averageHeight;

    private int x, y;

    private boolean indented;

    private boolean isSpace;

    private boolean isLast;


    PageGlyphImpl(Bitmap bitmap, PageGlyphInfo rect) {
        this.bitmap = bitmap;
        this.baseLineShift = rect.getBaselineShift();
        this.averageHeight = rect.getAverageHeight();
        this.x = rect.getX();
        this.y = rect.getY();
        this.indented = rect.isIndented();
        this.isSpace = rect.isSpace();
        this.isLast = rect.isLast();
        paint_debug.setStyle(Paint.Style.STROKE);
        paint_debug.setColor(Color.BLUE);
        paint_debug.setStrokeWidth(1);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public boolean isLast() {
        return isLast;
    }
    public boolean isSpace() {
        return isSpace;
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {

        //Log.v(getClass().getName(), "Baseline shift is " + baseLineShift);

        if (isSpace && context.isNewline()) {
            context.setNewline(false);
            return;
        }

        Canvas canvas = context.getCanvas();
        int __height = bitmap.getHeight();
        int __width = bitmap.getWidth();


        Point startPoint = context.getStartPoint();
        int currentBaseline = context.getCurrentBaseLine();
        if (currentBaseline == 0) {
            currentBaseline = (int)(__height * 1.3);

        }
        context.setLineHeight(averageHeight);

        //checking if currect glyph is within page content
        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin() ) {
            //if not - start new line
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());
            context.setNewline(true);
        }

        if(indented) {
            startPoint.set(context.getMargin() + averageHeight/2, startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());
        }
        context.setNewline(false);


        Rect __srcRect = new Rect(0, 0, __width, __height);
        Rect __dstRect = new Rect(startPoint.x ,
                currentBaseline + baseLineShift - (int)(__height * context.getZoom()),
                startPoint.x +(int)(__width * context.getZoom()),
                currentBaseline + baseLineShift);

        if(show) {
            canvas.drawBitmap(bitmap, __srcRect, __dstRect, paint);
            if (Constants.DEBUG) {
                canvas.drawRect(__dstRect, paint_debug);
            }

        }

        context.getStartPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), __dstRect.bottom);
        context.setCurrentBaseLine(currentBaseline);

        //bitmap = null;

    }

}
