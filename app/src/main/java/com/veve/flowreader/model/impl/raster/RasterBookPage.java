package com.veve.flowreader.model.impl.raster;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.djvu.DjvuBookPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 19.04.2018.
 */

class RasterBookPage implements BookPage {

    DjvuBookPage djvuBookPage;

    public RasterBookPage(BookPage page) {
        djvuBookPage = (DjvuBookPage)page;
    }

    @Override
    public PageGlyph getNextGlyph() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        Bitmap bitmap = null;
        List<Rect> glyphs = new ArrayList<Rect>();
        djvuBookPage.process(bitmap, glyphs);
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
