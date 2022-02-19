package com.veve.flowreader.model;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Format specific component designed to retrieve book pages for non-recognized (raster) book.
 */
public interface BookSource {

    Bitmap getPageBytes(int pageNumber);

    List<Bitmap> getReflownPageBytes(int pageNumber, DevicePageContext context, List<PageGlyphInfo> pageGlyphs);

    Bitmap getPageGrayscaleBytes(int pageNumber);

    @Deprecated
    /**
     * Use PageLayoutParser.getGlyphs instead
     */
    List<PageGlyph> getPageGlyphs(int pageNumber);

    @Deprecated
    /**
     * Use BookRecord.getPagesCount instead
     */
    int getPagesCount();

    @Deprecated
    /**
     * Use BookRecord.getBookTitle instead
     */
    String getBookTitle();

    void closeBook();

}
