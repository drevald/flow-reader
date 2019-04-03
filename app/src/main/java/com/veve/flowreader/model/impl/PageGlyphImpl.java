package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

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

    public PageGlyphImpl(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private int getBaselineShif() {

        Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8SC3);
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        Mat dst = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_32SC1);
        Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
        Core.rotate(dst, dst, Core.ROTATE_90_CLOCKWISE);
        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 15, 40);

        Mat invertcolormatrix= new Mat(dst.rows(),dst.cols(), dst.type(), new Scalar(255,255,255));
        Core.subtract(invertcolormatrix, dst, dst);

        Mat hist = new Mat();
        Core.reduce(dst, hist, 0, Core.REDUCE_SUM, CvType.CV_32SC1);
        int h = bitmap.getHeight();

        int baseLineShift = 0;

        for (int i = 0; i<2*h/5; i++) {
            double[] doubles = hist.get(0, i);
            double d1 = doubles[0];
            doubles = hist.get(0, i+1);
            double d2 = doubles[0];
            if (d1 > 0 && (d2/d1)> 2) {
                baseLineShift = i;

            }
        }

        hist.release();
        dst.release();
        mat.release();
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
            context.setLineHeight(36);
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
