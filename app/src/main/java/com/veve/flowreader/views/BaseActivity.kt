package com.veve.flowreader.views

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {

    companion object {
        private val COLOR_MATRIX_INVERTED = ColorMatrix(floatArrayOf(
            -0.4f, 0f, 0f, 0f, 255f,
            0f, -0.4f, 0f, 0f, 255f,
            0f, 0f, -0.4f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        private val COLOR_FILTER_SEPIA: ColorFilter = ColorMatrixColorFilter(COLOR_MATRIX_INVERTED)

        @JvmField var darkTheme: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.setLocalNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
    }

    open fun changeTheme(v: View) = changeTheme()

    protected fun changeTheme() {
        darkTheme = !darkTheme
        delegate.setLocalNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    protected fun isNightMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    override fun onResume() = super.onResume()

    protected fun createInvertedBitmap(src: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawBitmap(src, 0f, 0f, Paint().apply { colorFilter = COLOR_FILTER_SEPIA })
        return bitmap
    }
}
