package com.veve.flowreader.model.impl.mocksimple;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by ddreval on 15.01.2018.
 */

public class MockPageImpl implements BookPage {

    private String content;

    private int position;

    MockPageImpl(String aContent) {
        content = aContent;
        position = 0;
    }


    private PageGlyph getNextGlyph() {
        if (position++ < content.length())
            return new MockPageGlyphImpl(content.substring(position-1, position));
        return null;
    }


    public void reset() {
        position = 0;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        PageGlyph pageGlyph;

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

    @Override
    public Bitmap getAsReflownBitmap(DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
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
