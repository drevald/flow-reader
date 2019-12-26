package com.veve.flowreader.uitest;

import android.graphics.Bitmap;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;

public class RendererTest extends BookTest {

    @Test
    public void testCachedRenderer() {
        BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new CachedPageRendererImpl(booksCollection, bookRecord, bookSource);
        List<Bitmap> bitmaps = pageRenderer.renderPage(new DevicePageContextImpl(50), 1);
        assertNotNull(bitmaps);
    }

}
