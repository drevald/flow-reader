package com.veve.flowreader.model;

import android.graphics.Bitmap;

/**
 * Format specifi component designed to retrieve book data for non-recognized (raster) book.
 * Data should include page count, book title and page bitmap;
 */
public interface BookSource {

    Bitmap getPageBytes(int pageNumber);

    String getBookTitle();

    int getPagesCount();

}
