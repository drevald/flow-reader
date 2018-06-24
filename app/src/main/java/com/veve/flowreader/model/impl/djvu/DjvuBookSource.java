package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BookSource;

public class DjvuBookSource implements BookSource {

    DjvuBook djvuBook;

    public DjvuBookSource(String path) {
        djvuBook = new DjvuBook(path);
    }

    @Override
    public Bitmap getPageBytes(int pageNumber) {
        DjvuBookPage djvuBookPage = (DjvuBookPage)djvuBook.getPage(pageNumber);
        return djvuBookPage.getAsBitmap();
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