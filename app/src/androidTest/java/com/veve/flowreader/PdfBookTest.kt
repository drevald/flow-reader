package com.veve.flowreader

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.model.impl.pdf.PdfBook
import com.veve.flowreader.model.impl.pdf.PdfBookSource
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PdfBookTest {

    private lateinit var filePath: String
    private lateinit var context: Context

    @Before
    fun storeBook() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "sample.pdf")
        filePath = file.path
        val inputStream = context.resources.openRawResource(R.raw.sample)
        Utils.copy(inputStream, FileOutputStream(file))
    }

    @Test
    fun testGlyphCount() {
        val source = PdfBookSource(filePath)
        val pageGlyphs = source.getPageGlyphs(0)
        println(pageGlyphs.size)
        Assert.assertEquals(3309, pageGlyphs.size)
    }

    @Test
    fun testMetaData() {
        val book = PdfBook(filePath)
        Assert.assertEquals("", book.getAuthor())
        Assert.assertEquals("Sample Document", book.title)
    }
}
