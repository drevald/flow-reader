package com.veve.flowreader.model

data class PageGlyphInfo(
    val indented: Boolean,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val averageHeight: Int,
    val baselineShift: Int,
    val space: Boolean,
    val last: Boolean
) {
    fun isSpace() = space
    fun isLast() = last
    fun isIndented() = indented
}
