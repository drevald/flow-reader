package com.veve.flowreader.model

import android.graphics.Bitmap

interface BookPage {
    fun getAsBitmap(context: DevicePageContext): Bitmap
    fun getAsReflowedBitmap(context: DevicePageContext, pageGlyphs: List<PageGlyphInfo>): List<Bitmap>
    fun getAsGrayscaleBitmap(context: DevicePageContext): Bitmap
    fun getWidth(): Int
    fun getHeight(): Int
}
