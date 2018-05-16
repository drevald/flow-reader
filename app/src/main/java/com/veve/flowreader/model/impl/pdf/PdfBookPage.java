package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageSource;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import java.util.ArrayList;
import java.util.List;

public class PdfBookPage implements BookPage, PageSource {

    private Page page;

    private int dpi;

    public PdfBookPage(Page page){
        this.page = page;
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
        return AndroidDrawDevice.drawPage(page, context.getDisplayDpi());
    }

    @Override
    public Bitmap getAsBitmap() {
        DevicePageContext context = new DevicePageContextImpl();
        context.setDisplayDpi(72);
        return getAsBitmap(context);
    }

    @Override
    public List<Rect> getGlyphs() {
        List<Rect> list = new ArrayList<Rect>();
        Bitmap bitmap = getAsBitmap();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        list.add(rect);
        return list;
    }

    @Override
    public int getWidth() {
        return (int)(page.getBounds().x1 - page.getBounds().x0);
    }

    @Override
    public int getHeight() {
        return (int)(page.getBounds().y1 - page.getBounds().y0);
    }



}
