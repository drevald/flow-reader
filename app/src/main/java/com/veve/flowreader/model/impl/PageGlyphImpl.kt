package com.veve.flowreader.model.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import com.veve.flowreader.Constants
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyph
import com.veve.flowreader.model.PageGlyphInfo

class PageGlyphImpl(
    private val bitmap: Bitmap,
    rect: PageGlyphInfo
) : PageGlyph {

    private val baseLineShift: Int = rect.baselineShift
    val averageHeight: Int = rect.averageHeight
    val isIndented: Boolean = rect.indented
    private val x: Int = rect.x
    private val y: Int = rect.y
    private val isSpace: Boolean = rect.space
    private val isLast: Boolean = rect.last

    fun getX() = x
    fun getY() = y
    fun getWidth() = bitmap.width
    fun getHeight() = bitmap.height
    fun isLast() = isLast
    fun isSpace() = isSpace

    override fun draw(context: DevicePageContext, show: Boolean) {
        if (isSpace && context.isNewline()) {
            context.newline = false
            return
        }

        val canvas = context.canvas
        val glyphHeight = bitmap.height
        val glyphWidth = bitmap.width
        val startPoint = context.startPoint
        var currentBaseline = context.currentBaseLine
        if (currentBaseline == 0) {
            currentBaseline = (glyphHeight * 1.3).toInt()
        }
        context.lineHeight = averageHeight

        if (glyphWidth * context.zoom + startPoint.x > context.width - context.margin) {
            startPoint.set(
                (context.margin * 25).toInt(),
                startPoint.y + (glyphHeight * context.zoom).toInt() + (context.leading * context.zoom).toInt()
            )
            currentBaseline += (context.lineHeight * context.zoom).toInt() + (context.leading * context.zoom).toInt()
            context.newline = true
        }

        if (isIndented) {
            startPoint.set(
                (context.margin * 25).toInt() + averageHeight / 2,
                startPoint.y + (glyphHeight * context.zoom).toInt() + (context.leading * context.zoom).toInt()
            )
            currentBaseline += (context.lineHeight * context.zoom).toInt() + (context.leading * context.zoom).toInt()
        }
        context.newline = false

        val srcRect = Rect(0, 0, glyphWidth, glyphHeight)
        val dstRect = Rect(
            startPoint.x,
            currentBaseline + baseLineShift - (glyphHeight * context.zoom).toInt(),
            startPoint.x + (glyphWidth * context.zoom).toInt(),
            currentBaseline + baseLineShift
        )

        if (show) {
            canvas!!.drawBitmap(bitmap, srcRect, dstRect, paint)
            if (Constants.DEBUG) {
                canvas.drawRect(dstRect, paintDebug)
            }
        }

        context.startPoint.set(startPoint.x + dstRect.width() + (context.kerning * context.zoom).toInt(), startPoint.y)
        context.remotestPoint.set(startPoint.x + dstRect.width() + (context.kerning * context.zoom).toInt(), dstRect.bottom)
        context.currentBaseLine = currentBaseline
    }

    companion object {
        private val paint = Paint()
        private val paintDebug = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLUE
            strokeWidth = 1f
        }
    }
}
