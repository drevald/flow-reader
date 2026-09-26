package com.veve.flowreader.model.impl.pdf

import android.graphics.Bitmap
import android.util.Log
import com.veve.flowreader.model.BookSource
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyph
import com.veve.flowreader.model.PageGlyphInfo

class PdfBookSource @Throws(Exception::class) constructor(path: String) : BookSource {

    private val pdfBook = PdfBook(path)

    override fun getPageBytes(pageNumber: Int): Bitmap =
        (pdfBook.getPage(pageNumber) as PdfBookPage).getAsBitmap()!!

    override fun getReflownPageBytes(pageNumber: Int, context: DevicePageContext, pageGlyphs: List<PageGlyphInfo>): List<Bitmap> {
        var start = System.currentTimeMillis()
        val page = pdfBook.getPage(pageNumber) as PdfBookPage
        Log.v("PERF", "\t\tpdfBook.getPage($pageNumber) took ${System.currentTimeMillis() - start} ms")
        start = System.currentTimeMillis()
        val bitmaps = page.getAsReflowedBitmap(context, pageGlyphs)
        Log.v("PERF", "\t\tpdfBookPage.getAsReflowedBitmap($pageNumber) took ${System.currentTimeMillis() - start} ms")
        return bitmaps
    }

    @Deprecated("Use PageLayoutParser.getGlyphs instead")
    override fun getPageGlyphs(pageNumber: Int): List<PageGlyph> =
        (pdfBook.getPage(pageNumber) as PdfBookPage).getPageGlyphs()

    override fun getPageGrayscaleBytes(pageNumber: Int): Bitmap =
        (pdfBook.getPage(pageNumber) as PdfBookPage).getAsGrayscaleBitmap()

    @Deprecated("Use BookRecord.getPagesCount instead")
    override fun getPagesCount(): Int = pdfBook.getPagesCount()

    @Deprecated("Use BookRecord.getBookTitle instead")
    override fun getBookTitle(): String? = null

    override fun closeBook() = pdfBook.close()
}
