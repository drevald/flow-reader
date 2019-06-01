package com.veve.flowreader.model.impl.mocksimple;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by ddreval on 15.01.2018.
 */

public class MockPageImpl implements BookPage {

    private String content;

    private static final int MAX_GLYPHS = 1000;

    private int position = 0;

    public MockPageImpl(String aContent) {
        content = aContent;
        position = 0;
    }


    public PageGlyph getNextGlyph() {
        if (position++ < content.length())
            return new MockPageGlyphImpl(content.substring(position-1, position));
        return null;
    }


    public void reset() {
        position = 0;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        PageGlyph pageGlyph = null;

        while((pageGlyph = getNextGlyph()) != null) {
            pageGlyph.draw(context, false);
        }

        context.setCurrentBaseLine(0);
        context.setLineHeight(0);

        Point remotestPoint = context.getRemotestPoint();
        Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y, ARGB_8888);
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
        canvas.drawRect(0, 0, context.getWidth(), remotestPoint.y, paint);

        context.resetPosition();
        context.setCanvas(canvas);
        return bitmap;

    }


    public Bitmap getAsOriginalBitmap(DevicePageContext context) {
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