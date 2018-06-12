package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

public class PageGlyphImpl implements PageGlyph {

    private Bitmap bitmap;

    public PageGlyphImpl(Bitmap bitmap, Rect rect) {
        this.bitmap = bitmap;
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {

    }

}
