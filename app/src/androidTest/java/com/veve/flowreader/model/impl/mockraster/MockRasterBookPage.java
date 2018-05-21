package com.veve.flowreader.model.impl.mockraster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by ddreval on 2/16/2018.
 */

class MockRasterBookPage implements BookPage {

    private int pageNumber;

    private int position = 0;

    public MockRasterBookPage() {
        this.position = 0;
    }

    public MockRasterBookPage(int pageNumber) {
        this.position = 0;
        this.pageNumber = pageNumber;
    }

    @Override
    public PageGlyph getNextGlyph() {
        if (position++ < 1000) {
            return MockRasterBookPageGlyph.getInstance(); //new MockRasterBookPageGlyph();
        }
        return null;
    }

    @Override
    public void reset() {
        position = 0;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        PageGlyph pageGlyph = null;
        Log.i(getClass().getName(), String.format("position=%d", position));
        while((pageGlyph = getNextGlyph()) != null) {
            pageGlyph.draw(context, false);
        }
        Point remotestPoint = context.getRemotestPoint();
        Log.i(getClass().getName(), String.format("w=%d h=%d, position=%d", context.getWidth(), remotestPoint.y, position));
        Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y + (int)context.getLeading() , ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        reset();
        context.resetPosition();
        context.setCanvas(canvas);

        while((pageGlyph = getNextGlyph()) != null) {
            pageGlyph.draw(context, true);
        }

        Paint paint =  new Paint();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, context.getWidth(), remotestPoint.y + (int)context.getLeading(), paint);
        reset();
        context.resetPosition();
        context.setCanvas(canvas);
        return bitmap;

    }

    @Override
    public Bitmap getAsOriginalBitmap(DevicePageContext context) {
        return null;
    }

//    Bitmap bitmap = Bitmap.createBitmap(400, 400, ARGB_8888);
//
////        Canvas canvas = new Canvas(bitmap);
////        canvas.drawCircle(50, 50, 50, new Paint());
//
//    Canvas canvas = new Canvas(bitmap);
//        context.resetPosition();
//        context.setCanvas(canvas);
//    PageGlyph pageGlyph;
//    Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(10);
//
//
//        canvas.drawRect(canvas.getClipBounds(), paint);
//
//    position = 350;
//
//        while (position-- > 0)
//            new MockRasterBookPageGlyph().draw(context, true);


    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
