package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.veve.flowreader.Utils;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.PageSize;
import com.veve.flowreader.model.impl.AbstractBookPage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<Bitmap> getAsReflownBitmap(DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        PageSize pageSize = new PageSize();
        List<byte[]> bytes = getNativeReflownBytes(getBookId(), getPageNumber(), context.getZoom(), (int)(context.getWidth() * 0.8),
                pageSize, pageGlyphs, context.isPreprocessing(), context.getMargin());

        List<Bitmap> retVal = new ArrayList<>();

        for (int i=0;i<bytes.size(); i++) {
            byte[] b = bytes.get(i);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            //opts.inPreferredConfig = Bitmap.Config.ARGB_8887;
            opts.inJustDecodeBounds= true;
            BitmapFactory.decodeByteArray(b,0, b.length, opts);
            opts.inSampleSize = Utils.calculateInSampleSize(opts, context.getWidth(), context.getWidth()*(opts.outHeight/opts.outWidth));
            opts.inJustDecodeBounds= false;
            Bitmap bm = BitmapFactory.decodeByteArray(b,0, b.length, opts);

            retVal.add(bm);
            bytes.set(i, null);
        }
        bytes = null;

        return retVal;

    }

    @Override
    public int getWidth() {
        return getNativeWidth(getBookId(), getPageNumber());
    }

    @Override
    public int getHeight() {
        return getNativeHeight(getBookId(), getPageNumber());
    }

    private static native List<byte[]> getNativeReflownBytes(long bookId, int pageNumber, float scale, int pageWidth, PageSize pageSize, List<PageGlyphInfo> pageGlyphs, boolean preprocessing, float margin);

    private static native byte[] getNativeBytes(long bookId, int pageNumber);

    private static native byte[] getNativeGrayscaleBytes(long bookId, int pageNumber);

    private static native byte[] getNativePageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}

