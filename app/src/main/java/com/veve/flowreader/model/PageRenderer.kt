package com.veve.flowreader.model

import android.graphics.Bitmap

interface PageRenderer {
    fun renderPage(context: DevicePageContext, position: Int): List<Bitmap>
    fun renderOriginalPage(context: DevicePageContext, position: Int): Bitmap
    fun renderOriginalPage(position: Int): Bitmap
    fun setPageLayoutParser(parser: PageLayoutParser)
    fun closeBook()
}
