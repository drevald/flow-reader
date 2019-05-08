package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ddreval on 19.04.2018.
 */

public class PageGlyphImpl implements PageGlyph {

    private static Paint paint = new Paint();

    private Bitmap bitmap;

    private int baseLineShift;

    private int averageHeight;

    public PageGlyphImpl(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.baseLineShift = 0;
        this.averageHeight = 36;
    }

    public PageGlyphImpl(Bitmap bitmap, int baseLineShift, int averageHight) {
        this.bitmap = bitmap;
        this.baseLineShift = baseLineShift;
        this.averageHeight = averageHight;
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
        //checking if currect glyph is within page content
        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin()) {
            //if not - start new line
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
            currentBaseline += context.getLineHeight() * context.getZoom() + (int)(context.getLeading()* context.getZoom());
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
