package com.veve.flowreader.model

import android.graphics.Bitmap

/** Recognizes page layout (columns, lines, images) and parses it into glyphs of proper type. */
interface PageLayoutParser {
    fun getGlyphs(bitmap: Bitmap): List<PageGlyph>
    fun getGlyphs(bookSource: BookSource, position: Int): List<PageGlyph>
}
