package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;


/**
 * Created by ddreval on 19.04.2018.
 */

public class PageGlyphImpl implements PageGlyph {

    private static Paint paint = new Paint();

    private Bitmap bitmap;

    private int baseLineShift;

    private int averageHeight;

    private int x, y;


    public PageGlyphImpl(Bitmap bitmap, PageGlyphInfo rect) {
        this.bitmap = bitmap;
        this.baseLineShift = rect.getBaselineShift();
        this.averageHeight = rect.getAverageHeight();
        this.x = rect.getX();
        this.y = rect.getY();
    }

    public PageGlyphImpl(Bitmap bitmap, int baseLineShift, int averageHight, int x , int y) {
        this.bitmap = bitmap;
        this.baseLineShift = baseLineShift;
        this.averageHeight = averageHight;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private int getBaselineShif() {
        return baseLineShift;
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {

        int baseLineShift = getBaselineShif();

        Canvas canvas = context.getCanvas();
        int __height = bitmap.getHeight();
        int __width = bitmap.getWidth();


        Point startPoint = context.getStartPoint();
        int currentBaseline = context.getCurrentBaseLine();
        if (currentBaseline == 0) {
            currentBaseline = (int)(__height * 1.3);
            context.setLineHeight(averageHeight);
        }

        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin()) {
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            currentBaseline += context.getLineHeight();
        }
        Rect __srcRect = new Rect(0, 0, __width, __height);
        Rect __dstRect = new Rect(startPoint.x ,
                currentBaseline + baseLineShift - (int)(__height * context.getZoom()),
                startPoint.x +(int)(__width * context.getZoom()),
                currentBaseline + baseLineShift);

        if(show) {
            canvas.drawBitmap(bitmap, __srcRect, __dstRect, paint);
        }
        context.getStartPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y + __dstRect.height());

        context.setCurrentBaseLine(currentBaseline);

        //bitmap = null;
    }


}
