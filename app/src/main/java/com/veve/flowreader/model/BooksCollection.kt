package com.veve.flowreader.model

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.veve.flowreader.MD5
import com.veve.flowreader.dao.AppDatabase
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.dao.DaoAccess
import com.veve.flowreader.dao.PageGlyphRecord
import java.io.File
import java.util.concurrent.TimeUnit

class BooksCollection private constructor(context: Context) {

    private val daoAccess: DaoAccess = AppDatabase.getInstance(context).daoAccess()
    companion object {
        private var bookCollection: BooksCollection? = null

        @JvmStatic
        fun getInstance(context: Context): BooksCollection {
            if (bookCollection == null) {
                bookCollection = BooksCollection(context)
            }
            return bookCollection!!
        }
    }

    fun getBooks(): List<BookRecord>? {
        val task = BooksGetterTask(daoAccess)
        task.execute()
        return try {
            task.get()
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage)
            null
        }
    }

    fun getBook(id: Long): BookRecord? = getBook(id, true)

    fun getBook(id: Long, async: Boolean): BookRecord? {
        if (!async) return daoAccess.getBook(id)
        val task = BookGetterTask(daoAccess)
        task.execute(id)
        return try {
            task.get()
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage)
            null
        }
    }

    fun getBook(url: String): BookRecord? {
        Log.v("BOOK", "Getting by URL $url")
        val task = BookByUrlGetterTask(daoAccess)
        task.execute(url)
        return try {
            task.get()
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage)
            null
        }
    }

    fun deleteBook(bookId: Long) = BookDeleteTask(daoAccess).execute(bookId)

    fun deleteAllGlyphs(bookId: Long) {
        object : AsyncTask<Long, Void, Void>() {
            override fun doInBackground(vararg ids: Long?): Void? {
                daoAccess.deleteBookGlyphs(ids[0]!!)
                return null
            }
        }.execute(bookId)
    }

    fun getBookByChecksum(checksum: String?): BookRecord? {
        val task = BookGetChecksumTask(daoAccess)
        task.execute(checksum)
        return try {
            task.get()
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage)
            null
        }
    }

    fun hasBook(file: File): Boolean {
        val task = BookCheckerTask(daoAccess)
        task.execute(file)
        return try {
            task.get()
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage)
            false
        }
    }

    fun addBook(bookRecord: BookRecord): Long = daoAccess.addBook(bookRecord)

    fun updateBook(bookRecord: BookRecord) = BookUpdateTask(daoAccess).execute(bookRecord)

    fun getPageGlyphs(id: Long, position: Int, async: Boolean): List<PageGlyphRecord>? {
        if (!async) return daoAccess.getPageGlyphs(id, position)
        val task = GetPageGlyphsTask(daoAccess)
        Log.v(javaClass.name, "1")
        task.execute(id, position)
        return try {
            Log.v(javaClass.name, "2")
            task.get(1, TimeUnit.MINUTES)
        } catch (e: Exception) {
            Log.e(javaClass.name, "Failed to get glyphs for book #$id page $position", e)
            null
        }
    }

    fun addGlyphs(glyphsToStore: List<PageGlyphRecord>, async: Boolean) {
        if (!async) {
            daoAccess.insertGlyphs(glyphsToStore)
            return
        }
        try {
            AddPageGlyphsTask(daoAccess).execute(glyphsToStore)
        } catch (e: Exception) {
            Log.e(javaClass.name, "Failed to insert glyphs", e)
        }
    }

    fun deleteGlyphs(bookId: Long, position: Long) = daoAccess.deleteBookPageGlyphs(bookId, position)
}

///////////////////////   DB TASKS   ////////////////////////////////////////////

private class AddPageGlyphsTask(private val daoAccess: DaoAccess) :
    AsyncTask<List<PageGlyphRecord>, Void, Void>() {
    override fun doInBackground(vararg lists: List<PageGlyphRecord>): Void? {
        daoAccess.insertGlyphs(lists[0])
        return null
    }
}

private class GetPageGlyphsTask(private val daoAccess: DaoAccess) :
    AsyncTask<Any, Void, List<PageGlyphRecord>>() {
    override fun doInBackground(vararg objects: Any): List<PageGlyphRecord> =
        daoAccess.getPageGlyphs(objects[0] as Long, objects[1] as Int)
}

private class BookUpdateTask(private val daoAccess: DaoAccess) :
    AsyncTask<BookRecord, Void, Void>() {
    override fun doInBackground(vararg bookRecords: BookRecord): Void? {
        daoAccess.updateBook(bookRecords[0])
        return null
    }
}

private class BookDeleteTask(private val daoAccess: DaoAccess) :
    AsyncTask<Long, Void, Void>() {
    override fun doInBackground(vararg longs: Long?): Void? {
        daoAccess.deleteBook(longs[0]!!)
        daoAccess.deleteBookGlyphs(longs[0]!!)
        return null
    }
}

private class BookGetterTask(private val daoAccess: DaoAccess) :
    AsyncTask<Long, Void, BookRecord?>() {
    override fun doInBackground(vararg ids: Long?): BookRecord? = daoAccess.getBook(ids[0]!!)
}

private class BooksGetterTask(private val daoAccess: DaoAccess) :
    AsyncTask<Void, Void, List<BookRecord>>() {
    override fun doInBackground(vararg voids: Void): List<BookRecord> = daoAccess.listBooks()
}

private class BookCheckerTask(private val daoAccess: DaoAccess) :
    AsyncTask<File, Void, Boolean>() {
    override fun doInBackground(vararg files: File): Boolean {
        val checksum = MD5.fileToMD5(files[0].path)
        return daoAccess.fetchBookByChecksum(checksum ?: "") != null
    }
}

private class BookByUrlGetterTask(private val daoAccess: DaoAccess) :
    AsyncTask<String, Void, BookRecord?>() {
    override fun doInBackground(vararg strings: String): BookRecord? = daoAccess.fetchBook(strings[0])
}

private class BookGetChecksumTask(private val daoAccess: DaoAccess) :
    AsyncTask<String, Void, BookRecord?>() {
    override fun doInBackground(vararg strings: String): BookRecord? =
        daoAccess.fetchBookByChecksum(strings[0])
}
