package com.veve.flowreader.uitest;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.NativePageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;

public class RendererTest extends BookTest {

    @Test
    public void testCachedRenderer() {
        BookSource bookSource = new PdfBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new CachedPageRendererImpl(booksCollection, bookRecord, bookSource);
        List<Bitmap> bitmaps = pageRenderer.renderPage(new DevicePageContextImpl(50), 1);
        assertNotNull(bitmaps);
    }

    @Test
    public void testNativeRenderer() {
        BookSource bookSource = new PdfBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new NativePageRendererImpl(booksCollection, bookRecord, bookSource);
        List<Bitmap> bitmaps = pageRenderer.renderPage(new DevicePageContextImpl(50), 1);
        assertNotNull(bitmaps);
    }

    @Test
    public void testNativeOriginalPageRenderer() {
        BookSource bookSource = new PdfBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new NativePageRendererImpl(booksCollection, bookRecord, bookSource);
        Bitmap bitmap = pageRenderer.renderOriginalPage(1);
        assertNotNull(bitmap);
    }

}
