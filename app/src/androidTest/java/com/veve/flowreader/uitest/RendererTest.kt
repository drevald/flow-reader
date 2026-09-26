package com.veve.flowreader.uitest

import android.graphics.Bitmap
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.impl.NativePageRendererImpl
import com.veve.flowreader.model.impl.pdf.PdfBookSource
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class RendererTest : BookTest() {

    @Test
    fun testNativeRenderer() {
        val bookSource = PdfBookSource(bookRecord.url)
        val pageRenderer = NativePageRendererImpl(booksCollection, bookRecord, bookSource)
        val bitmaps = pageRenderer.renderPage(DevicePageContext(50), 1)
        assertNotNull(bitmaps)
    }

    @Test
    fun testNativeOriginalPageRenderer() {
        val bookSource = PdfBookSource(bookRecord.url)
        val pageRenderer = NativePageRendererImpl(booksCollection, bookRecord, bookSource)
        val bitmap: Bitmap = pageRenderer.renderOriginalPage(1)
        assertNotNull(bitmap)
    }
}
