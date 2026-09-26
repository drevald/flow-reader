package com.veve.flowreader.model

interface PageGlyph {
    /** Draws glyph on page and updates context to place next glyph. */
    fun draw(context: DevicePageContext, show: Boolean)
}
