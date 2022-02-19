package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.github.axet.k2pdfopt.K2PdfOpt;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.PageSize;
import com.veve.flowreader.model.impl.AbstractBookPage;

import java.util.ArrayList;
import java.util.List;

public class PdfBookPage extends AbstractBookPage implements BookPage {


    PdfBookPage(long bookId, int pageNumber) {
       super(bookId, pageNumber);
    }

    public Bitmap getAsBitmap() {
        Log.v("NULLBOOK", "Getting page as bitmap");
        byte[] imageBytes= getBytes(getBookId(), getPageNumber());
        Log.v("NULLBOOK", "page bytes " + imageBytes);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opts.inJustDecodeBounds= false;
        Log.v("NULLBOOK", "Decoding " + imageBytes.length + " bytes");
        Bitmap bm = BitmapFactory.decodeByteArray(imageBytes,0, imageBytes.length, opts);
        Log.v("NULLBOOK", "Bitmap ready");
        return bm;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return getAsBitmap();
    }



    @Override
    public byte[] getGrayscaleBytes(long bookId, int pageNumber) {
        return getNativeGrayscaleBytes(bookId,pageNumber);
    }

    @Override
    public byte[] getBytes(long bookId, int pageNumber) {
        return getNativeBytes(bookId,pageNumber);
    }

    @Override
    public byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs) {
        return getNativePageGlyphs(bookId, pageNumber, pageGlyphs);
    }

    @Override
    public int getWidth() {
        return getNativeWidth(getBookId(), getPageNumber());
    }

    @Override
    public int getHeight() {
        return getNativeHeight(getBookId(), getPageNumber());
    }

    @Override
    public List<Bitmap> getAsReflowedBitmap(DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        PageSize pageSize = new PageSize();

        List<byte[]> bytes = getNativeReflowedBytes(getBookId(), getPageNumber(), context.getZoom(),
                (int)(context.getWidth() * magicMultiplier), pageSize, pageGlyphs, context.isPreprocessing(), context.getMargin());

        List<Bitmap> retVal = new ArrayList<>();

        int bitmapWidth = 0;
        int totalHeight = 0;

        for (int i=0;i<bytes.size(); i++) {
            byte[] b = bytes.get(i);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            //opts.inPreferredConfig = Bitmap.Config.ARGB_8887;
            opts.inJustDecodeBounds= true;

            BitmapFactory.decodeByteArray(b,0, b.length, opts);
            int h = opts.outHeight;
            int w = opts.outWidth;
            opts.inSampleSize = calculateInSampleSize(opts, context.getWidth(), context.getWidth()*(opts.outHeight/opts.outWidth));

            opts.inJustDecodeBounds= false;
            Bitmap bm = BitmapFactory.decodeByteArray(b,0, b.length, opts);
            Log.d("FLOW-READER", "bitmap height = " + bm.getHeight());

            retVal.add(bm);
            totalHeight += bm.getHeight();
            bitmapWidth = bm.getWidth();
            bytes.set(i, null);
        }
        bytes = null;

        if (context.isWillusSegmentation()) {
            return getWillusBitmap(retVal, context.getWidth(), bitmapWidth, totalHeight);
        }


        return retVal;


        /*
        int width = pageSize.getPageWidth();
        int height = pageSize.getPageHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ALPHA_8;
        final ByteBuffer bb = ByteBuffer.allocateDirect(bytes.length);
        bb.put(bytes);
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap(" + width + ", " + height + ", bitmapConfig);");
        Bitmap bm = Bitmap.createBitmap(width, height, bitmapConfig);
        bb.rewind();
        bm.copyPixelsFromBuffer(bb);
        return bm;

         */
    }


    private static native List<byte[]> getNativeReflowedBytes(long bookId, int pageNumber, float scale, int pageWidth, PageSize pageSize, List<PageGlyphInfo> pageGlyphs, boolean preprocessing, float margin);

    private static native int getNativeWidth(long bookId, int pageNumber);

    private static native int getNativeHeight(long bookId, int pageNumber);

    private static native byte[] getNativeBytes(long bookId, int pageNumber);

    private static native byte[] getNativeGrayscaleBytes(long bookId, int pageNumber);

    private static native byte[] getNativePageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
