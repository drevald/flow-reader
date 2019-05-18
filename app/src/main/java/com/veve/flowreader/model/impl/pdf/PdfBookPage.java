package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageSource;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRegion;
import com.veve.flowreader.model.impl.PageUtil;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfBookPage implements BookPage {

    private Page page;

    private int dpi;

    public PdfBookPage(Page page){
        this.page = page;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return AndroidDrawDevice.drawPage(page, context.getDisplayDpi());
    }

    @Override
    public List<PageGlyph> getPageGlyphs(DevicePageContext context) {
        return null;
    }

    public Bitmap getAsBitmap() {
        DevicePageContext context = new DevicePageContextImpl();
        context.setDisplayDpi(144);
        return getAsBitmap(context);
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
