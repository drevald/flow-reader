package com.veve.flowreader.model.impl.raster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageSource;
import com.veve.flowreader.model.impl.djvu.DjvuBookPage;
import com.veve.flowreader.model.impl.djvu.DjvuBookPageGlyph;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by ddreval on 19.04.2018.
 */

class RasterBookPage implements BookPage {

    List<Rect> glyphs;

    PageSource sourcePage;

    Bitmap bitmap;

    int position = 0;

    public RasterBookPage(PageSource page) {
        sourcePage = page;
        bitmap = sourcePage.getAsBitmap();
        glyphs = sourcePage.getGlyphs();
    }

    @Override
    public PageGlyph getNextGlyph() {
        if (position>glyphs.size()-1) {
            return null; // fake restriction
        }
        Rect rect = glyphs.get(position++);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
        return new DjvuBookPageGlyph(newBitmap);
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
        context.setCurrentBaseLine(0);
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

        reset();
        context.resetPosition();
        context.setCanvas(canvas);
        return bitmap;

    }

    @Override
    public Bitmap getAsOriginalBitmap(DevicePageContext context) {
        return bitmap;
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
