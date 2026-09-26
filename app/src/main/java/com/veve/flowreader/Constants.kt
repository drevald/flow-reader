package com.veve.flowreader

import android.graphics.Color

object Constants {
    const val VIEW_MODE_PHONE = 1
    const val VIEW_MODE_ORIGINAL = 2
    const val VIEW_MODE_PARAM = "viewMode"

    @JvmField val LIGHT_PINK = Color.rgb(255, 64, 129)
    @JvmField val GREEN = Color.rgb(0, 64, 0)
    const val BOOK_THUMB_WIDTH = 100
    const val BOOK_THUMB_HOR_PADDING = 20
    const val VIEW_TYPE = "VIEW_TYPE"
    const val LIST_VIEW_TYPE = 0
    const val GRID_VIEW_TYPE = 1
    const val DEBUG = false
    const val MAX_BITMAP_SIZE = 100 * 1024 * 1024
    const val IMAGE_VIEW_HEIGHT_LIMIT = 4000
    const val BOOK_CONTEXT = "BOOK_CONTEXT"
    const val BOOK_ID = "BOOK_ID"
    const val POSITION = "POSITION"
    const val FILE_NAME = "FILENAME"
    const val MARGIN_STEP = 50
    const val MARGIN_MAX = 250
    const val ZOOM_STEP = 0.5F
    const val ZOOM_MIN = 1.5F
    const val ZOOM_MAX = 5F
    const val PREFERENCES = "PREFERENCES"
    const val FLOW_BOOK_PREFERENCES = "FLOW_BOOK_PREFERENCES"
    const val SHOW_TRY_REFLOW = "SHOW_TRY_REFLOW"
    const val KINDLE_NAVIGATION = "KINDLE_NAVIGATION"
    const val PINCH_ZOOM = "PINCH_ZOOM"
    const val SHOW_SCROLLBARS = "SHOW_SCROLLBARS"
    const val JUSTIFY_TEXT = "JUSTIFY_TEXT"
}
