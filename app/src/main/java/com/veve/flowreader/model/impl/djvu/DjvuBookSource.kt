package com.veve.flowreader.model.impl.djvu

import android.graphics.Bitmap
import android.util.Log
import com.veve.flowreader.model.BookSource
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyph
import com.veve.flowreader.model.PageGlyphInfo

class DjvuBookSource @Throws(Exception::class) constructor(path: String) : BookSource {

    private val djvuBook = DjvuBook(path)

    override fun getPageBytes(pageNumber: Int): Bitmap =
        (djvuBook.getPage(pageNumber) as DjvuBookPage).getAsBitmap()!!

    override fun getReflownPageBytes(pageNumber: Int, context: DevicePageContext, pageGlyphs: List<PageGlyphInfo>): List<Bitmap> {
        var start = System.currentTimeMillis()
        val page = djvuBook.getPage(pageNumber) as DjvuBookPage
        Log.v("PERF", "\t\tdjvuBook.getPage($pageNumber) took ${System.currentTimeMillis() - start} ms")
        start = System.currentTimeMillis()
        val bitmaps = page.getAsReflowedBitmap(context, pageGlyphs)
        Log.v("PERF", "\t\tdjvuBookPage.getAsReflowedBitmap($pageNumber) took ${System.currentTimeMillis() - start} ms")
        return bitmaps
    }

    override fun getPageGrayscaleBytes(pageNumber: Int): Bitmap =
        (djvuBook.getPage(pageNumber) as DjvuBookPage).getAsGrayscaleBitmap()

    @Deprecated("Use PageLayoutParser.getGlyphs instead")
    override fun getPageGlyphs(pageNumber: Int): List<PageGlyph> =
        (djvuBook.getPage(pageNumber) as DjvuBookPage).getPageGlyphs()

    @Deprecated("Use BookRecord.getBookTitle instead")
    override fun getBookTitle(): String = djvuBook.getName()

    override fun closeBook() = djvuBook.close()

    @Deprecated("Use BookRecord.getPagesCount instead")
    override fun getPagesCount(): Int = djvuBook.getPagesCount()
}
