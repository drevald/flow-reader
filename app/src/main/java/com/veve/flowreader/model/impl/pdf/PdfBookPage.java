package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;

import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

public class PdfBookPage implements BookPage {

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
    public int getWidth() {
        return (int)(page.getBounds().x1 - page.getBounds().x0);
    }

    @Override
    public int getHeight() {
        return (int)(page.getBounds().y1 - page.getBounds().y0);
    }
}
