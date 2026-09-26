package com.veve.flowreader.model

import android.graphics.Canvas
import android.graphics.Point
import android.util.Log
import com.veve.flowreader.Constants
import java.io.Serializable

class DevicePageContext : Serializable {

    companion object {
        private const val DEFAULT_ZOOM = 5.0f
        private const val DEFAULT_FONT_SIZE = 24f
        private const val DEFAULT_LEADING = 0.5f * DEFAULT_FONT_SIZE
        private const val DEFAULT_KERNING = 0.5f
        private const val DEFAULT_MARGIN = 25
        private const val DEFAULT_DISPLAY_DPI = 72
    }

    @Transient var canvas: Canvas? = null
    var zoom: Float = DEFAULT_ZOOM
    var zoomOriginal: Float = DEFAULT_ZOOM
    var zoomMin: Float = Constants.ZOOM_MIN
    var zoomMax: Float = Constants.ZOOM_MAX
    var zoomStep: Float = Constants.ZOOM_STEP
    var width: Int = 0
        get() {
            Log.v(javaClass.name, "Width for ${hashCode()} after update $field")
            return field
        }
        set(value) {
            Log.v(javaClass.name, "Width for ${hashCode()} before update $field")
            field = value
            Log.v(javaClass.name, "Width for ${hashCode()} after update $field")
        }
    var lineHeight: Int = 0
    var kerning: Float = DEFAULT_KERNING
    var leading: Float = DEFAULT_LEADING
    var margin: Float = 1.0f
    var currentBaseLine: Int = 0
    var newline: Boolean = false
    var preprocessing: Boolean = false
    var invalidateCache: Boolean = false
    var breakWords: Boolean = false
    var showGlyphBorders: Boolean = false
    var portrait: Boolean = false
    var screenRatio: Float = 0f
    var resolution: Int = 0

    @Transient var remotestPoint: Point = Point()
    @Transient var startPoint: Point = Point()

    constructor(width: Int) {
        this.zoom = DEFAULT_ZOOM
        this.zoomOriginal = DEFAULT_ZOOM
        this.kerning = DEFAULT_KERNING
        this.leading = DEFAULT_LEADING
        this.width = width
        this.margin = 1.0f
        this.currentBaseLine = 0
        this.remotestPoint = Point((margin * DEFAULT_MARGIN).toInt(), 0)
        this.startPoint = Point((margin * DEFAULT_MARGIN).toInt(), 0)
    }

    constructor() {
        newline = true
        this.remotestPoint = Point()
        this.startPoint = Point()
    }

    fun setZoomLimits(medianGlyphBaseHeight: Float, xdpi: Float) {
        if (medianGlyphBaseHeight <= 0) return
        val dpi = if (xdpi > 0) xdpi else DEFAULT_DISPLAY_DPI.toFloat()
        zoomMin = 4f * dpi / (72f * medianGlyphBaseHeight)
        zoomMax = 16f * dpi / (72f * medianGlyphBaseHeight)
        zoomStep = 1f * dpi / (72f * medianGlyphBaseHeight)
    }

    fun isNewline(): Boolean = newline
    fun isPreprocessing(): Boolean = preprocessing
    fun isInvalidateCache(): Boolean = invalidateCache
    fun isBreakWords(): Boolean = breakWords
    fun isShowGlyphBorders(): Boolean = showGlyphBorders

    fun resetPosition() {
        startPoint = Point((margin * DEFAULT_MARGIN).toInt(), 0)
        remotestPoint = Point((margin * DEFAULT_MARGIN).toInt(), 0)
    }
}
