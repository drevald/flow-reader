package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.github.axet.k2pdfopt.K2PdfOpt;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBookPage implements BookPage {

    private int pageNumber;
    private long bookId;

    protected double magicMultiplier = 0.8;

    public AbstractBookPage(long  bookId, int pageNumber) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
    }


    @Override
    public Bitmap getAsGrayscaleBitmap(DevicePageContext context) {
        return getAsGrayscaleBitmap();
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return getAsBitmap();
    }

    public List<PageGlyph> getPageGlyphs() {

        List<PageGlyphInfo> pageGlyphInfos = new ArrayList<>();

        Bitmap.Config bitmapConfig = Bitmap.Config.ALPHA_8;
        int width = getWidth();
        int height = getHeight();

        byte[] bytes = getPageGlyphs(bookId, pageNumber, pageGlyphInfos);

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        Log.d(getClass().getName(), String.format("Creating bitmap %d x %d", width, height));
        Bitmap bm = Bitmap.createBitmap(width, height, bitmapConfig);
        byteBuffer.rewind();
        bm.copyPixelsFromBuffer(byteBuffer);

        List<PageGlyph> pageGlyphs = new ArrayList<>();

        Bitmap bitmap;
        for (PageGlyphInfo pageGlyphInfo :pageGlyphInfos) {
            int x = pageGlyphInfo.getX();
            int y = pageGlyphInfo.getY();
            int w = pageGlyphInfo.getWidth();
            int h = pageGlyphInfo.getHeight();
            bitmap = Bitmap.createBitmap(bm,x,y,w,h);
            PageGlyph pg = new PageGlyphImpl(bitmap, pageGlyphInfo);
            pageGlyphs.add(pg);
        }

        return pageGlyphs;
    }


    public Bitmap getAsBitmap() {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        final ByteBuffer bb = ByteBuffer.wrap(imageBytes);
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap(" + width + ", " + height + ", bitmapConfig);");
        Bitmap bm = Bitmap.createBitmap(width, height, bitmapConfig);
        bb.rewind();
        bm.copyPixelsFromBuffer(bb);
        return bm;
    }

    public Bitmap getAsGrayscaleBitmap() {

        byte[] imageBytes= getGrayscaleBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ALPHA_8;
        final ByteBuffer bb = ByteBuffer.wrap(imageBytes);
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap(" + width + ", " + height + ", bitmapConfig);");
        Bitmap bm = Bitmap.createBitmap(width, height, bitmapConfig);
        bb.rewind();
        bm.copyPixelsFromBuffer(bb);
        return bm;
    }


    public int getPageNumber() {
        return pageNumber;
    }

    public long getBookId() {
        return bookId;
    }

    abstract public byte[] getBytes(long bookId, int pageNumber);

    abstract public byte[] getGrayscaleBytes(long bookId, int pageNumber);

    abstract public byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    public List<Bitmap> getWillusBitmap(List<Bitmap> retVal, int width, int bitmapWidth, int totalHeight) {

        Bitmap wholeBitmap = Bitmap.createBitmap(bitmapWidth, totalHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(wholeBitmap);

        int drawHeight = 0;
        for (int i=0; i<retVal.size(); i++) {
            Bitmap b = retVal.get(i);
            if (i==0) {
                canvas.drawBitmap(b, new Matrix(), new Paint());
            } else {
                canvas.drawBitmap(b, 0, drawHeight, new Paint());
            }
            drawHeight += b.getHeight();
        }

        K2PdfOpt opt = new K2PdfOpt();
        opt.create(width, totalHeight, 400);

        opt.load(wholeBitmap);
        wholeBitmap.recycle();

        List<Bitmap> bitmaps = new ArrayList<>();


        for (int i=0; i<opt.getCount(); i++) {
            Bitmap bm = opt.renderPage(i);
            Bitmap b = Bitmap.createScaledBitmap(bm, width, totalHeight, true);
            bitmaps.add(b);
            totalHeight += b.getHeight();
            bm.recycle();
        }

        return bitmaps;

        }


}
