package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;
import android.util.Log;

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
    public List<Bitmap> getReflownPageBytes(int pageNumber, DevicePageContext context, List<PageGlyphInfo> pageGlyphs) {
        long start = System.currentTimeMillis();
        PdfBookPage pdfBookPage = (PdfBookPage)pdfBook.getPage(pageNumber);
        Log.v("PERF", String.format("\t\tpdfBook.getPage(%d) took %d ms", pageNumber, System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        List<Bitmap> bitmaps = pdfBookPage.getAsReflowedBitmap(context, pageGlyphs);
        Log.v("PERF", String.format("\t\tpdfBookPage.getAsReflowedBitmap(%d) took %d ms", pageNumber, System.currentTimeMillis() - start));
        return bitmaps;
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

    @Override
    public void closeBook() {
        pdfBook.close();
    }

}
