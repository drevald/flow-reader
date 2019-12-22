package com.veve.flowreader.model.impl.mockraster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by ddreval on 2/16/2018.
 */

class MockRasterBookPage implements BookPage {


    private int position;

    MockRasterBookPage() {
        this.position = 0;
    }

    private PageGlyph getNextGlyph() {
        if (position++ < 1000) {
            return MockRasterBookPageGlyph.getInstance(); //new MockRasterBookPageGlyph();
        }
        return null;
    }


    public void reset() {
        position = 0;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        PageGlyph pageGlyph;
        Log.i(getClass().getName(), String.format("position=%d", position));
        while((pageGlyph = getNextGlyph()) != null) {
            pageGlyph.draw(context, false);
        }

        context.setCurrentBaseLine(0);
        context.setLineHeight(0);

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

        context.setCurrentBaseLine(0);
        context.setLineHeight(0);

        Paint paint =  new Paint();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, context.getWidth(), remotestPoint.y + (int)context.getLeading(), paint);
        reset();
        context.resetPosition();
        context.setCanvas(canvas);

        try {
            Thread.sleep((int)(Math.random() * 5000));
        } catch (Exception e) {
            Log.d(getClass().getName(), e.getLocalizedMessage());
        }

        return bitmap;

    }

    @Override
    public List<Bitmap> getAsReflownBitmap(DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        return null;
    }


    @Override
    public Bitmap getAsGrayscaleBitmap(DevicePageContext context) {
        return null;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }


}
