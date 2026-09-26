package com.veve.flowreader

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.text.MessageFormat

object BookContentResolver {
    private val fileInputFormat = MessageFormat("file://{0}")
    private val contentInputFormat = MessageFormat("{0}_files{1}")
    private val outputFormat = MessageFormat("/storage/emulated/0{0}")

    @JvmStatic
    @Throws(Exception::class)
    fun contentToFile(context: Context, uri: Uri): String? {
        var path: String? = null
        try {
            path = when (uri.scheme) {
                SCHEME_CONTENT -> {
                    val inputStr = uri.encodedPath
                    outputFormat.format(arrayOf(contentInputFormat.parse(inputStr)[1]))
                }
                SCHEME_FILE -> {
                    fileInputFormat.parse(uri.toString())[0] as String
                }
                else -> null
            }
            path = URLDecoder.decode(path, "UTF-8")
        } catch (e: Exception) {
            Log.v(BookContentResolver::class.java.name, "Opening $uri")
            val uriString = URLDecoder.decode(uri.toString(), "UTF-8")
            val fileName = uriString.substring(1 + uriString.lastIndexOf('/'))
            val file: File = when {
                uriString.lowercase().endsWith(".pdf") || uriString.lowercase().endsWith(".djvu") ->
                    File(context.getExternalFilesDir(null), fileName)
                else -> {
                    val cR: ContentResolver = context.contentResolver
                    val mime = MimeTypeMap.getSingleton()
                    val type = mime.getExtensionFromMimeType(cR.getType(uri))
                    when (type) {
                        "djvu" -> File(context.getExternalFilesDir(null), "$fileName.djvu")
                        "pdf"  -> File(context.getExternalFilesDir(null), "$fileName.pdf")
                        else   -> File.createTempFile("flow.", null, context.getExternalFilesDir(null))
                    }
                }
            }
            context.contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(file).use { output ->
                    Utils.copy(input!!, output)
                }
            }
            path = file.path
        }
        return path
    }
}
