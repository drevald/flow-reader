package com.veve.flowreader.model

import android.graphics.Bitmap

interface BookSource {
    fun getPageBytes(pageNumber: Int): Bitmap
    fun getReflownPageBytes(pageNumber: Int, context: DevicePageContext, pageGlyphs: List<PageGlyphInfo>): List<Bitmap>
    fun getPageGrayscaleBytes(pageNumber: Int): Bitmap

    @Deprecated("Use PageLayoutParser.getGlyphs instead")
    fun getPageGlyphs(pageNumber: Int): List<PageGlyph>

    @Deprecated("Use BookRecord.getPagesCount instead")
    fun getPagesCount(): Int

    @Deprecated("Use BookRecord.getBookTitle instead")
    fun getBookTitle(): String?

    fun closeBook()
}
