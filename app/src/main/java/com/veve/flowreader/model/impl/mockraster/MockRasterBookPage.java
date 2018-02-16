package com.veve.flowreader.model.impl.mockraster;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

/**
 * Created by ddreval on 2/16/2018.
 */

class MockRasterBookPage implements BookPage {

    @Override
    public PageGlyph getNextGlyph() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return null;
    }

}
