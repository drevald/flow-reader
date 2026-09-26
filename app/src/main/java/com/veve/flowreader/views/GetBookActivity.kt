package com.veve.flowreader.views

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.veve.flowreader.BookContentResolver
import com.veve.flowreader.Constants
import com.veve.flowreader.MD5
import com.veve.flowreader.R
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BookFactory
import com.veve.flowreader.model.BooksCollection
import java.io.File

class GetBookActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_book)
        val uri: Uri? = intent.data
        Log.v("TEST", "Uri arrived $uri")
        try {
            val booksCollection = BooksCollection.getInstance(applicationContext)
            val file = File(BookContentResolver.contentToFile(applicationContext, uri!!)!!)
            val checksum = MD5.fileToMD5(this, file.path)
            val bookRecord = booksCollection.getBookByChecksum(checksum)
            if (bookRecord != null) {
                Log.v("TEST", "Book found id:${bookRecord.id}, url:${bookRecord.url}, md5:${bookRecord.md5}, title:${bookRecord.title}")
                startActivity(Intent(this, PageActivity::class.java).apply {
                    putExtra(Constants.BOOK_ID, bookRecord.id)
                    putExtra(Constants.FILE_NAME, bookRecord.url)
                })
            } else {
                BookCreatorTask().execute(file)
            }
        } catch (e: Exception) {
            Log.e(javaClass.name, "URI is $uri", e)
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            })
        }
    }

    inner class BookCreatorTask : AsyncTask<File, Void, Void>() {
        private var newBook: BookRecord? = null
        private var bookId: Long = 0
        private var errorMessage: String? = null

        override fun doInBackground(vararg files: File): Void? {
            val bookFile = files[0]
            Log.v(javaClass.name, "Start parsing new book at ${bookFile.path}")
            try {
                newBook = BookFactory.getInstance().createBook(bookFile)
                bookId = BooksCollection.getInstance(applicationContext).addBook(newBook!!)
                Log.v("BOOK", "Inserted with URL ${newBook!!.url}")
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to open file"
                Log.e(javaClass.name, "Failed to open book: $errorMessage")
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (errorMessage != null) {
                AlertDialog.Builder(this@GetBookActivity)
                    .setTitle("Cannot open file")
                    .setMessage(errorMessage)
                    .setPositiveButton(R.string.ok) { d, _ -> finish() }
                    .setOnCancelListener { finish() }
                    .show()
                return
            }
            startActivity(Intent(this@GetBookActivity, PageActivity::class.java).apply {
                putExtra(Constants.BOOK_ID, bookId)
                putExtra(Constants.FILE_NAME, newBook!!.url)
            })
        }
    }
}
