package com.veve.flowreader.model.impl

import android.graphics.Bitmap
import android.util.Log
import com.veve.flowreader.model.BookPage
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyph
import com.veve.flowreader.model.PageGlyphInfo
import java.nio.ByteBuffer

abstract class AbstractBookPage protected constructor(
    val bookId: Long,
    val pageNumber: Int
) : BookPage {

    protected var magicMultiplier = 0.8

    override fun getAsGrayscaleBitmap(context: DevicePageContext): Bitmap = getAsGrayscaleBitmap()

    override fun getAsBitmap(context: DevicePageContext): Bitmap = getAsBitmap()!!

    fun getPageGlyphs(): List<PageGlyph> {
        val pageGlyphInfos = ArrayList<PageGlyphInfo>()
        val bitmapConfig = Bitmap.Config.ALPHA_8
        val width = getWidth()
        val height = getHeight()
        val bytes = getPageGlyphs(bookId, pageNumber, pageGlyphInfos)
        val byteBuffer = ByteBuffer.wrap(bytes)
        Log.d(javaClass.name, "Creating bitmap $width x $height")
        val bm = Bitmap.createBitmap(width, height, bitmapConfig)
        byteBuffer.rewind()
        bm.copyPixelsFromBuffer(byteBuffer)
        return pageGlyphInfos.map { info ->
            val bitmap = Bitmap.createBitmap(bm, info.x, info.y, info.width, info.height)
            PageGlyphImpl(bitmap, info)
        }
    }

    open fun getAsBitmap(): Bitmap? {
        val imageBytes = getBytes(bookId, pageNumber) ?: return null
        val width = getWidth()
        val height = getHeight()
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap($width, $height, bitmapConfig);")
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        ByteBuffer.wrap(imageBytes).also { bm.copyPixelsFromBuffer(it) }
        return bm
    }

    fun getAsGrayscaleBitmap(): Bitmap {
        val imageBytes = getGrayscaleBytes(bookId, pageNumber)
        val width = getWidth()
        val height = getHeight()
        Log.v("BITMAP_MEMORY", "Bitmap.createBitmap($width, $height, bitmapConfig);")
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        ByteBuffer.wrap(imageBytes).also { bm.copyPixelsFromBuffer(it) }
        return bm
    }

    public abstract fun getBytes(bookId: Long, pageNumber: Int): ByteArray?
    public abstract fun getGrayscaleBytes(bookId: Long, pageNumber: Int): ByteArray
    public abstract fun getPageGlyphs(bookId: Long, pageNumber: Int, pageGlyphs: MutableList<PageGlyphInfo>): ByteArray
}
