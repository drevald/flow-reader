package com.veve.flowreader.model.impl.djvu;

import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.impl.AbstractBookPage;
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
    public String getTitle() {
        return getNativeTitle(getBookId());
    }

    @Override
    public String getAuthor() {
        return getNativeAuthor(getBookId());
    }

    private static native String getNativeTitle(long bookId);

    private static native String getNativeAuthor(long bookId);

    private static native byte[] getNativeBytes(long bookId, int pageNumber);

    private static native byte[] getNativePageGlyphs(long bookId, int pageNumber, List<PageGlyphInfo> pageGlyphs);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}
