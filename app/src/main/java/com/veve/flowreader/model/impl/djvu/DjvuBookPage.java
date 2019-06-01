package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.impl.PageGlyphImpl;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuBookPage implements BookPage {

    private int pageNumber;
    private long bookId;

    public DjvuBookPage(long  bookId, int pageNumber) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return getAsBitmap();
    }

    @Override
    public List<PageGlyph> getPageGlyphs(DevicePageContext context) {
        return getPageGlyphs();
    }

    public List<PageGlyph> getPageGlyphs() {

        long start = System.currentTimeMillis();
        List<PageGlyphInfo> pageGlyphInfos = new ArrayList<>();

        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        int width = getWidth();
        int height = getHeight();

        byte[] bytes = getPageGlyphs(bookId, pageNumber, pageGlyphInfos);

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byteBuffer.rewind();
        bm.copyPixelsFromBuffer(byteBuffer);

        List<PageGlyph> pageGlyphs = new ArrayList<>();

        for (PageGlyphInfo pageGlyphInfo :pageGlyphInfos) {
            int x = pageGlyphInfo.getX();
            int y = pageGlyphInfo.getY();
            int w = pageGlyphInfo.getWidth();
            int h = pageGlyphInfo.getHeight();
            //Mat image = new Mat(mat, new Rect(x,y, w, h));
            //Bitmap bitmap = Bitmap.createBitmap(w, h, bitmapConfig);
            Bitmap bitmap = Bitmap.createBitmap(bm,x,y,w,h);
            //Utils.matToBitmap(image, bitmap);
            PageGlyph pg = new PageGlyphImpl(bitmap, pageGlyphInfo);
            pageGlyphs.add(pg);
        }

        Log.d("DJVU1", "Java time "+ (System.currentTimeMillis() - start));

        return pageGlyphs;
    }


    public Bitmap getAsBitmap() {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;


        final ByteBuffer bb = ByteBuffer.wrap(imageBytes);

        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bb.rewind();
        bm.copyPixelsFromBuffer(bb);
        return bm;
    }

    @Override
    public int getWidth() {
        return getNativeWidth(bookId, pageNumber);
    }

    @Override
    public int getHeight() {
        return getNativeHeight(bookId, pageNumber);
    }

    private static native byte[] getBytes(long bookId, int pageNumber);

    private static native byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}
