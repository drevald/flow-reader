package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.PageSize;
import com.veve.flowreader.model.impl.AbstractBookPage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;


public class DjvuBookPage extends AbstractBookPage implements BookPage  {

    DjvuBookPage(long  bookId, int pageNumber) {
        super(bookId, pageNumber);
    }

    @Override
    public byte[] getBytes(long bookId, int pageNumber) {
        return getNativeBytes(bookId,pageNumber);
    }

    @Override
    public byte[] getGrayscaleBytes(long bookId, int pageNumber) {
        return getNativeGrayscaleBytes(bookId,pageNumber);
    }

    @Override
    public byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs) {
        return getNativePageGlyphs(bookId, pageNumber, pageGlyphs);
    }


    @Override
    public Bitmap getAsReflownBitmap(DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        PageSize pageSize = new PageSize();
        byte[] bytes = getNativeReflownBytes(getBookId(), getPageNumber(), context.getZoom(), pageSize, pageGlyphs);
        int width = pageSize.getPageWidth();
        int height = pageSize.getPageHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ALPHA_8;
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap(" + width + ", " + height + ", bitmapConfig);");
        Bitmap bm = Bitmap.createBitmap(width, height, bitmapConfig);
        bb.rewind();
        bm.copyPixelsFromBuffer(bb);
        return bm;
    }

    @Override
    public int getWidth() {
        return getNativeWidth(getBookId(), getPageNumber());
    }

    @Override
    public int getHeight() {
        return getNativeHeight(getBookId(), getPageNumber());
    }

    private static native byte[] getNativeReflownBytes(long bookId, int pageNumber, float scale, PageSize pageSize, List<PageGlyphInfo> pageGlyphs);

    private static native byte[] getNativeBytes(long bookId, int pageNumber);

    private static native byte[] getNativeGrayscaleBytes(long bookId, int pageNumber);

    private static native byte[] getNativePageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}
