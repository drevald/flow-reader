package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

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
    public PageGlyph getNextGlyph() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;

        int[] bitmapPixels = new int[width * height];
        for (int i = 0, size = bitmapPixels.length; i < size; ++i) {

            bitmapPixels[i] = Color.rgb(imageBytes[3*i], imageBytes[3*i+1],imageBytes[3*i+2]);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        bitmap.setPixels(bitmapPixels, 0, width, 0, 0, width, height);
        return bitmap;
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

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);
}
