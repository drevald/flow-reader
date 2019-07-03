package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;


/**
 * Created by ddreval on 19.04.2018.
 */

public class PageGlyphImpl implements PageGlyph {

    private static Paint paint = new Paint();

    private static Paint paint_debug = new Paint();

    private Bitmap bitmap;

    private int baseLineShift;

    private int averageHeight;

    private int x, y;

    private boolean indented;


    public PageGlyphImpl(Bitmap bitmap, PageGlyphInfo rect) {
        this.bitmap = bitmap;
        this.baseLineShift = rect.getBaselineShift();
        this.averageHeight = rect.getAverageHeight();
        this.x = rect.getX();
        this.y = rect.getY();
        this.indented = rect.isIndented();
        paint_debug.setStyle(Paint.Style.STROKE);
        paint_debug.setColor(Color.BLUE);
        paint_debug.setStrokeWidth(1);
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
        Log.v(getClass().getName(), "Baseline shift is " + baseLineShift);

        Canvas canvas = context.getCanvas();
        int __height = bitmap.getHeight();
        int __width = bitmap.getWidth();


        Point startPoint = context.getStartPoint();
        int currentBaseline = context.getCurrentBaseLine();
        if (currentBaseline == 0) {
            currentBaseline = (int)(__height * 1.3);
            context.setLineHeight(averageHeight);
        }

        String showLine = show?"Paint ":"Measure ";

        //checking if currect glyph is within page content
        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin() ) {
            //if not - start new line
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            Log.v(getClass().getName(), showLine + "New line - startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom()) + (int)(context.getLeading()* context.getZoom()));");
            Log.v(getClass().getName(), String.format(showLine + "New line - startPoint.set(%d, %d + (int)(%d * %f) + (int)(%f * ));",
                    context.getMargin(), startPoint.y, __height, context.getZoom(), context.getLeading(), context.getZoom()));

            currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());
            Log.v(getClass().getName(), showLine + "New line - currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());");
            Log.v(getClass().getName(), String.format(showLine + "New line - currentBaseline += %d * %f + (int)(%f * %f)",
                    context.getLineHeight(), context.getZoom(), context.getLeading(), context.getZoom()));
        }
        if(indented) {
            //if not - start new line
            startPoint.set(context.getMargin() + averageHeight/2, startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            Log.v(getClass().getName(), showLine + "Indented - startPoint.set(context.getMargin() + averageHeight/2, startPoint.y + (int)(__height * context.getZoom()) + (int)(context.getLeading()* context.getZoom()))");
            Log.v(getClass().getName(), String.format(showLine + "Indented - startPoint.set(%d + %d/2,%d + (int)(%d * %f) + (int)(%f* %f))",
                    context.getMargin(), averageHeight,  startPoint.y, __height, context.getZoom(), context.getLeading(), context.getZoom()));
            currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());
            Log.v(getClass().getName(), showLine + "Indented - currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());");
            Log.v(getClass().getName(), String.format(showLine + "Indented - currentBaseline += %d * %f + (int)(%f * %f)",
                    context.getLineHeight(), context.getZoom(), context.getLeading(), context.getZoom()));
        }
        Rect __srcRect = new Rect(0, 0, __width, __height);
        Rect __dstRect = new Rect(startPoint.x ,
                currentBaseline + baseLineShift - (int)(__height * context.getZoom()),
                startPoint.x +(int)(__width * context.getZoom()),
                currentBaseline + baseLineShift);
        Log.v(getClass().getName(), String.format(showLine + "Rect __dstRect = new Rect(startPoint.x , currentBaseline + baseLineShift - (int)(__height * context.getZoom()),startPoint.x +(int)(__width * context.getZoom()),currentBaseline + baseLineShift);"));
        Log.v(getClass().getName(), String.format(showLine + "Rect __dstRect = new Rect(%d , %d + %d - (int)(%d * %f),%d +(int)(%d * %f), %d + %d);",
                startPoint.x, currentBaseline, baseLineShift, __height, context.getZoom(), startPoint.x, __width, context.getZoom(), currentBaseline, baseLineShift));

        if(show) {
            canvas.drawBitmap(bitmap, __srcRect, __dstRect, paint);
            canvas.drawRect(__dstRect, paint_debug);
        }
        context.getStartPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y + __dstRect.height());

        Log.v(getClass().getName(), showLine + "context.getStartPoint().set(startPoint.x + __dstRect.width() + (int)(context.getKerning()* context.getZoom()), startPoint.y);");
        Log.v(getClass().getName(), String.format(showLine + "context.getStartPoint().set(%d + %d + (int)(%f* %f), %d);",
                startPoint.x, __dstRect.width(), context.getKerning(), context.getZoom(), startPoint.y));

        Log.v(getClass().getName(), showLine + "context.getRemotestPoint().set(startPoint.x + __dstRect.width() + (int)(context.getKerning()* context.getZoom()), startPoint.y + __dstRect.height());");
        Log.v(getClass().getName(), String.format(showLine + "context.getRemotestPoint().set(%d + %d + (int)(%f* %f), %d + %d);",
                startPoint.x, __dstRect.width(), context.getKerning(), context.getZoom(), startPoint.y, __dstRect.height()));

        context.setCurrentBaseLine(currentBaseline);

        //bitmap = null;
    }


}
