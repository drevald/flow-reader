package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.util.Log;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;

import java.util.List;

public class DjvuBookSource implements BookSource {

    private DjvuBook djvuBook;

    public DjvuBookSource(String path) {
        djvuBook = new DjvuBook(path);
    }

    @Override
    public Bitmap getPageBytes(int pageNumber) {
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        return djvuBookPage.getAsBitmap();
    }

    @Override
    public List<Bitmap> getReflownPageBytes(int pageNumber, DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        long start = System.currentTimeMillis();
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        Log.v("PERF", String.format("\t\tdjvuBook.getPage(%d) took %d ms", pageNumber, System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        List<Bitmap> bitmaps = djvuBookPage.getAsReflowedBitmap(context, pageGlyphs);
        Log.v("PERF", String.format("\t\tdjvuBookPage.getAsReflowedBitmap(%d) took %d ms", pageNumber, System.currentTimeMillis() - start));
        return bitmaps;
    }

    @Override
    public Bitmap getPageGrayscaleBytes(int pageNumber) {
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        return djvuBookPage.getAsGrayscaleBitmap();

    }

    @Override
    public List<PageGlyph> getPageGlyphs(int pageNumber) {
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        return djvuBookPage.getPageGlyphs();
    }

    @Override
    public String getBookTitle() {
        return djvuBook.getName();
    }

    @Override
    public void closeBook() {
        djvuBook.close();
    }

    @Override
    public int getPagesCount() {
        return djvuBook.getPagesCount();
    }

}
