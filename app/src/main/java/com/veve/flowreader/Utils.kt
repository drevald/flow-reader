package com.veve.flowreader

import android.app.Application
import android.graphics.BitmapFactory
import java.io.InputStream
import java.io.OutputStream

class Utils : Application() {
    companion object {
        @JvmStatic
        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

        @JvmStatic
        @Synchronized
        fun copy(input: InputStream, output: OutputStream) {
            val buffer = ByteArray(1000)
            var len: Int
            while (input.read(buffer).also { len = it } != -1) {
                output.write(buffer, 0, len)
            }
            output.close()
            input.close()
        }
    }
}
