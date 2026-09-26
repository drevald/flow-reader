package com.veve.flowreader.model.impl.djvu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.veve.flowreader.Utils
import com.veve.flowreader.model.BookPage
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyphInfo
import com.veve.flowreader.model.PageSize
import com.veve.flowreader.model.impl.AbstractBookPage

class DjvuBookPage internal constructor(bookId: Long, pageNumber: Int) :
    AbstractBookPage(bookId, pageNumber), BookPage {

    companion object {
        @JvmStatic private external fun getNativeReflowedBytes(bookId: Long, pageNumber: Int, scale: Float, pageWidth: Int, pageSize: PageSize, pageGlyphs: List<PageGlyphInfo>, preprocessing: Boolean, margin: Float, breakOnSpace: Boolean, showGlyphBorders: Boolean): List<ByteArray>
        @JvmStatic private external fun getNativeBytes(bookId: Long, pageNumber: Int): ByteArray
        @JvmStatic private external fun getNativeGrayscaleBytes(bookId: Long, pageNumber: Int): ByteArray
        @JvmStatic private external fun getNativePageGlyphs(bookId: Long, pageNumber: Int, pageGlyphs: List<PageGlyphInfo>): ByteArray
        @JvmStatic private external fun getNativeWidth(bookId: Long, pageNumber: Int): Int
        @JvmStatic private external fun getNativeHeight(bookId: Long, pageNumber: Int): Int
    }

    override fun getBytes(bookId: Long, pageNumber: Int): ByteArray = getNativeBytes(bookId, pageNumber)
    override fun getGrayscaleBytes(bookId: Long, pageNumber: Int): ByteArray = getNativeGrayscaleBytes(bookId, pageNumber)
    override fun getPageGlyphs(bookId: Long, pageNumber: Int, pageGlyphs: MutableList<PageGlyphInfo>): ByteArray =
        getNativePageGlyphs(bookId, pageNumber, pageGlyphs)

    override fun getAsReflowedBitmap(context: DevicePageContext, pageGlyphs: List<PageGlyphInfo>): List<Bitmap> {
        val pageSize = PageSize()
        val bytes = getNativeReflowedBytes(
            bookId, pageNumber, context.zoom,
            (context.width * magicMultiplier).toInt(),
            pageSize, pageGlyphs, context.isPreprocessing(),
            context.margin, !context.isBreakWords(), context.isShowGlyphBorders()
        )
        val retVal = mutableListOf<Bitmap>()
        for (i in bytes.indices) {
            val b = bytes[i]
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(b, 0, b.size, opts)
            opts.inSampleSize = Utils.calculateInSampleSize(opts, context.width, context.width * (opts.outHeight / opts.outWidth))
            opts.inJustDecodeBounds = false
            retVal.add(BitmapFactory.decodeByteArray(b, 0, b.size, opts))
        }
        return retVal
    }

    override fun getWidth(): Int = getNativeWidth(bookId, pageNumber)
    override fun getHeight(): Int = getNativeHeight(bookId, pageNumber)
}
