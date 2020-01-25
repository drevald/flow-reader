package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;

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
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        return djvuBookPage.getAsReflowedBitmap(context, pageGlyphs);
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
    public int getPagesCount() {
        return djvuBook.getPagesCount();
    }

}
