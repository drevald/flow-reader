package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.util.Log;

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

    public AbstractBookPage(long  bookId, int pageNumber) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return getAsBitmap();
    }

    public List<PageGlyph> getPageGlyphs() {

        List<PageGlyphInfo> pageGlyphInfos = new ArrayList<>();

        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
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

    abstract public byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);
}
