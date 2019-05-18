package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.impl.PageGlyphImpl;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

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

        List<PageGlyphInfo> pageGlyphInfos = new ArrayList<>();
        byte[] imageBytes = getPageGlyphs(bookId, pageNumber, pageGlyphInfos);

        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Mat mat = new Mat(height, width ,CvType.CV_8UC3);
        mat.put(0,0,imageBytes, 0, imageBytes.length);

        List<PageGlyph> pageGlyphs = new ArrayList<>();

        for (PageGlyphInfo pageGlyphInfo :pageGlyphInfos) {
            int x = pageGlyphInfo.getX();
            int y = pageGlyphInfo.getY();
            int w = pageGlyphInfo.getWidth();
            int h = pageGlyphInfo.getHeight();
            Mat image = new Mat(mat, new Rect(x,y, w, h));
            Bitmap bitmap = Bitmap.createBitmap(w, h, bitmapConfig);
            Utils.matToBitmap(image, bitmap);
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
        Mat mat = new Mat(height, width ,CvType.CV_8UC3);
        mat.put(0,0,imageBytes, 0, imageBytes.length);

        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Utils.matToBitmap(mat, bitmap);
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

    private static native byte[] getPageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}
