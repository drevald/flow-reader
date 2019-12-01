package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.impl.djvu.DjvuBookPage;

import java.util.List;

public class PdfBookSource implements BookSource {

    private PdfBook pdfBook;

    public PdfBookSource(String path) {
        pdfBook = new PdfBook(path);
    }

    @Override
    public Bitmap getPageBytes(int pageNumber) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        return pdfBookPage.getAsBitmap();
    }

    @Override
    public Bitmap getReflownPageBytes(int pageNumber, DevicePageContext context, List<PageGlyphInfo> pageGlyphs, boolean preprocessing) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        return pdfBookPage.getAsReflownBitmap(context, pageGlyphs, preprocessing);
    }

    @Override
    public List<PageGlyph> getPageGlyphs(int pageNumber) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        return pdfBookPage.getPageGlyphs();
    }

    @Override
    public Bitmap getPageGrayscaleBytes(int pageNumber) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        return pdfBookPage.getAsGrayscaleBitmap();

    }

    @Override
    public int getPagesCount() {
        return pdfBook.getPagesCount();
    }

    @Override
    public String getBookTitle() {
        return null;
    }

}
