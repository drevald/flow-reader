package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.PageGlyph;

import java.util.List;

public class PdfBookSource implements BookSource {

    PdfBook pdfBook;

    public PdfBookSource(String path) {
        pdfBook = new PdfBook(path);
    }

    @Override
    public Bitmap getPageBytes(int pageNumber) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        return pdfBookPage.getAsBitmap();
    }

    @Override
    public List<PageGlyph> getPageGlyphs(int pageNumber) {
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        List<PageGlyph> pageGlyphs = pdfBookPage.getPageGlyphs();
        return pageGlyphs;
    }

    @Override
    public String getBookTitle() {
        return pdfBook.getName();
    }

    @Override
    public int getPagesCount() {
        return pdfBook.getPagesCount();
    }

}
