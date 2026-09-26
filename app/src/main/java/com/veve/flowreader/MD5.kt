package com.veve.flowreader

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest

object MD5 {
    @JvmStatic
    fun fileToMD5(filePath: String): String? =
        try {
            FileInputStream(filePath).use { computeMD5(it) }
        } catch (e: Exception) {
            null
        }

    @JvmStatic
    fun fileToMD5(context: Context, filePath: String): String? =
        try {
            context.contentResolver.openInputStream(Uri.fromFile(File(filePath)))?.use { computeMD5(it) }
        } catch (e: Exception) {
            null
        }

    private fun computeMD5(inputStream: InputStream): String {
        val buffer = ByteArray(1024)
        val digest = MessageDigest.getInstance("MD5")
        var numRead: Int
        while (inputStream.read(buffer).also { numRead = it } != -1) {
            if (numRead > 0) digest.update(buffer, 0, numRead)
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }.uppercase()
    }
}
