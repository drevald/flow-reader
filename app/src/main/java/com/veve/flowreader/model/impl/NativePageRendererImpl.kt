package com.veve.flowreader.model.impl

import android.graphics.Bitmap
import android.util.Log
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.dao.PageGlyphRecord
import com.veve.flowreader.model.BookSource
import com.veve.flowreader.model.BooksCollection
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageGlyphInfo
import com.veve.flowreader.model.PageLayoutParser
import com.veve.flowreader.model.PageRenderer

/** Component designed to render book page. */
class NativePageRendererImpl(
    private val booksCollection: BooksCollection,
    private var bookRecord: BookRecord,
    private val bookSource: BookSource
) : PageRenderer {

    private var originalBitmap: Bitmap? = null
    private var currentOriginalPage: Int = 0
    private var memGlyphs: List<PageGlyphRecord>? = null
    private fun updateZoomLimitsFromGlyphs(glyphs: List<PageGlyphInfo>, context: DevicePageContext) {
        val heights = glyphs
            .filter { !it.isSpace() && it.averageHeight >= 8 && it.averageHeight <= 200 }
            .map { it.averageHeight.toFloat() }
            .sorted()
        if (heights.isEmpty()) return
        val medianBaseHeight = heights[heights.size / 2]
        bookRecord.medianGlyphBaseHeight = medianBaseHeight
        context.setZoomLimits(medianBaseHeight, context.resolution.toFloat())
        val initialZoom = 10f * context.zoomStep
        context.zoom = initialZoom
        bookRecord.zoom = initialZoom
        booksCollection.updateBook(bookRecord)
        Log.d("ZOOM_LIMITS", "medianBaseHeight=%.2f zoomMin=%.2f zoomMax=%.2f initialZoom=%.2f"
            .format(medianBaseHeight, context.zoomMin, context.zoomMax, initialZoom))
    }

    private fun getOriginalPageBitmap(position: Int): Bitmap {
        Log.v("NULLBOOK", "Getting natively the original page $position")
        if (position != currentOriginalPage || originalBitmap == null) {
            Log.d("NULLBOOK", "Page not cached current = $currentOriginalPage requested = $position")
            currentOriginalPage = position
            val start = System.currentTimeMillis()
            originalBitmap = bookSource.getPageBytes(position)
            Log.d(javaClass.name, "NULLBOOK Getting page #$position took #${System.currentTimeMillis() - start} milliseconds")
        } else {
            Log.d(javaClass.name, "NULLBOOK Page cached current = $currentOriginalPage requested = $position")
        }
        return originalBitmap!!
    }

    private fun getReflownPageBitmap(position: Int, context: DevicePageContext): List<Bitmap> {
        var start = System.currentTimeMillis()
        if (context.isInvalidateCache()) {
            booksCollection.deleteGlyphs(bookRecord.id!!, position.toLong())
        }
        Log.v("PERF", "\tbooksCollection.deleteGlyphs(${bookRecord.id}, $position) took ${System.currentTimeMillis() - start} ms")

        start = System.currentTimeMillis()
        val storedGlyphs: List<PageGlyphRecord>?
        if (currentOriginalPage == position) {
            storedGlyphs = memGlyphs
        } else {
            storedGlyphs = booksCollection.getPageGlyphs(bookRecord.id!!, position, false)
            memGlyphs = storedGlyphs
            currentOriginalPage = position
        }
        Log.v("PERF", "\tbooksCollection.getPageGlyphs(${bookRecord.id}, $position, false) took ${System.currentTimeMillis() - start} ms")

        start = System.currentTimeMillis()
        if (storedGlyphs.isNullOrEmpty()) {
            val glyphs = mutableListOf<PageGlyphInfo>()
            var reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs)
            val glyphsToStore = glyphs.map { g ->
                PageGlyphRecord(bookRecord.id!!, position, g.x, g.y, g.width, g.height,
                    g.baselineShift, g.averageHeight, g.indented, g.isSpace(), g.isLast())
            }
            booksCollection.addGlyphs(glyphsToStore, false)
            if (bookRecord.medianGlyphBaseHeight == 0f) {
                updateZoomLimitsFromGlyphs(glyphs, context)
                reflownPageBytes.forEach { if (!it.isRecycled) it.recycle() }
                glyphs.clear()
                reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs)
            }
            Log.v("PERF", "\tbookSource.getReflownPageBytes($position, ...) took ${System.currentTimeMillis() - start} ms")
            Log.v(javaClass.name, "new reflownPageBytes.size()=${reflownPageBytes.size} for page $position")
            return reflownPageBytes
        } else {
            val glyphs = storedGlyphs.map { r ->
                PageGlyphInfo(r.indented, r.x, r.y, r.width, r.height,
                    r.averageHeight, r.baselineShift, r.isSpace, r.isLast)
            }
            if (bookRecord.medianGlyphBaseHeight == 0f) {
                updateZoomLimitsFromGlyphs(glyphs, context)
            }
            val reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs)
            Log.v("PERF", "\tbookSource.getReflownPageBytes($position, ...) took ${System.currentTimeMillis() - start} ms")
            Log.v(javaClass.name, "reflownPageBytes.size()=${reflownPageBytes.size} for page $position")
            return reflownPageBytes
        }
    }

    override fun renderPage(context: DevicePageContext, position: Int): List<Bitmap> {
        val start = System.currentTimeMillis()
        val bitmaps = getReflownPageBitmap(position, context)
        Log.v("PERF", "getReflownPageBitmap($position, context) took ${System.currentTimeMillis() - start} ms")
        return ArrayList(bitmaps)
    }

    override fun renderOriginalPage(context: DevicePageContext, position: Int): Bitmap {
        val bitmap = getOriginalPageBitmap(position)
        return Bitmap.createScaledBitmap(
            bitmap,
            (context.zoomOriginal * context.width).toInt(),
            (context.zoomOriginal * (context.width * bitmap.height) / bitmap.width).toInt(),
            false
        )
    }

    override fun renderOriginalPage(position: Int): Bitmap = getOriginalPageBitmap(position)

    override fun setPageLayoutParser(parser: PageLayoutParser) { /* do nothing */ }

    override fun closeBook() = bookSource.closeBook()
}
