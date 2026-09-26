package com.veve.flowreader.model.impl.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.veve.flowreader.model.BookPage
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyphInfo
import com.veve.flowreader.model.PageSize
import com.veve.flowreader.model.impl.AbstractBookPage

class PdfBookPage internal constructor(bookId: Long, pageNumber: Int) :
    AbstractBookPage(bookId, pageNumber), BookPage {

    companion object {
        @JvmStatic private external fun getNativeReflowedBytes(bookId: Long, pageNumber: Int, scale: Float, pageWidth: Int, pageSize: PageSize, pageGlyphs: List<PageGlyphInfo>, preprocessing: Boolean, margin: Float, breakOnSpace: Boolean, showGlyphBorders: Boolean): List<ByteArray>
        @JvmStatic private external fun getNativeWidth(bookId: Long, pageNumber: Int): Int
        @JvmStatic private external fun getNativeHeight(bookId: Long, pageNumber: Int): Int
        @JvmStatic private external fun getNativeBytes(bookId: Long, pageNumber: Int): ByteArray?
        @JvmStatic private external fun getNativeGrayscaleBytes(bookId: Long, pageNumber: Int): ByteArray
        @JvmStatic private external fun getNativePageGlyphs(bookId: Long, pageNumber: Int, pageGlyphs: List<PageGlyphInfo>): ByteArray
    }

    override fun getAsBitmap(): Bitmap? {
        val imageBytes = getBytes(bookId, pageNumber) ?: return null
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        })
    }

    override fun getAsBitmap(context: DevicePageContext): Bitmap = getAsBitmap()!!

    override fun getGrayscaleBytes(bookId: Long, pageNumber: Int): ByteArray = getNativeGrayscaleBytes(bookId, pageNumber)
    override fun getBytes(bookId: Long, pageNumber: Int): ByteArray? = getNativeBytes(bookId, pageNumber)
    override fun getPageGlyphs(bookId: Long, pageNumber: Int, pageGlyphs: MutableList<PageGlyphInfo>): ByteArray =
        getNativePageGlyphs(bookId, pageNumber, pageGlyphs)

    override fun getWidth(): Int = getNativeWidth(bookId, pageNumber)
    override fun getHeight(): Int = getNativeHeight(bookId, pageNumber)

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
            opts.inSampleSize = calculateInSampleSize(opts, context.width, context.width * (opts.outHeight / opts.outWidth))
            opts.inJustDecodeBounds = false
            val bm = BitmapFactory.decodeByteArray(b, 0, b.size, opts)
            Log.d("FLOW-READER", "bitmap height = ${bm.height}")
            retVal.add(bm)
            bytes[i] // allow GC
        }
        return retVal
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
