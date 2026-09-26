package com.veve.flowreader.views

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import java.io.File
import java.io.FileFilter

class BrowseFilesActivity : BaseActivity() {

    private lateinit var fileListAdapter: FileListAdapter
    private lateinit var progress: ProgressBar

    companion object {
        private const val EXTERNAL_MEMORY_DIR = "/storage/sdcard1/"
    }

    override fun onResume() {
        super.onResume()
        progress.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_files)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        progress = findViewById(R.id.progress)

        findViewById<View>(R.id.external_memory).setOnClickListener {
            fileListAdapter.setRoot(EXTERNAL_MEMORY_DIR)
            fileListAdapter.notifyDataSetChanged()
        }

        if (File(EXTERNAL_MEMORY_DIR).exists()) {
            findViewById<View>(R.id.internal_memory).visibility = View.VISIBLE
            findViewById<View>(R.id.external_memory).visibility = View.VISIBLE
        }

        findViewById<FloatingActionButton>(R.id.home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        fileListAdapter = FileListAdapter()
        val listView = findViewById<ListView>(android.R.id.list)
        listView.adapter = fileListAdapter
        listView.onItemClickListener = FileListener(fileListAdapter)
    }

    inner class FileListAdapter : BaseAdapter() {
        private lateinit var rootDir: File
        var currentDirectory: File? = null
        val currentFiles = mutableListOf<File>()

        init { setRoot(Environment.getExternalStorageDirectory().absolutePath) }

        private fun sortFiles() {
            currentFiles.sortWith { a, b ->
                when {
                    a.isDirectory && b.isFile -> -1
                    a.isFile && b.isDirectory -> 1
                    else -> a.name.lowercase().compareTo(b.name.lowercase())
                }
            }
        }

        fun setRoot(path: String) {
            try {
                rootDir = File(path)
                currentDirectory = rootDir
                currentFiles.clear()
                val filter = FileFilter { f ->
                    (f.isFile && (f.toString().lowercase().endsWith("pdf") || f.toString().lowercase().endsWith("djvu"))) || f.isDirectory
                }
                currentDirectory!!.listFiles(filter)?.forEach { if (it.canRead() && !it.isHidden) currentFiles.add(it) }
                sortFiles()
            } catch (e: Exception) { e.printStackTrace() }
        }

        fun setRoot(index: Int) {
            val newRoot = currentFiles[index]
            if (!newRoot.isDirectory) return
            if (!newRoot.canRead()) {
                Log.i(javaClass.name, "Sorry, you can not read ${newRoot.absolutePath}")
                return
            }
            currentFiles.clear()
            val filter = FileFilter { f ->
                (f.isFile && (f.toString().lowercase().endsWith("pdf") || f.toString().lowercase().endsWith("djvu"))) || f.isDirectory
            }
            newRoot.listFiles(filter)?.forEach { if (it.canRead() && !it.isHidden) currentFiles.add(it) }
            if (rootDir != newRoot) currentFiles.add(0, newRoot.parentFile!!)
            sortFiles()
            currentDirectory = newRoot
            notifyDataSetChanged()
        }

        override fun getCount() = currentFiles.size
        override fun getItem(i: Int) = currentFiles[i]
        override fun getItemId(i: Int) = currentFiles[i].hashCode().toLong()

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.items_list, container, false)
            val textView = view as TextView
            val file = currentFiles[position]
            textView.text = if (position == 0 && currentDirectory != rootDir) ".." else file.name
            textView.setTextColor(when {
                !file.canRead() -> Constants.LIGHT_PINK
                file.canWrite() -> Constants.GREEN
                else -> Color.BLACK
            })
            textView.setTypeface(null, if (file.isDirectory) Typeface.BOLD else Typeface.NORMAL)
            return view
        }
    }

    inner class FileListener(private val fileListAdapter: FileListAdapter) : AdapterView.OnItemClickListener {
        override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
            val file = fileListAdapter.currentFiles[i]
            if (file.isFile) {
                if (!file.name.lowercase().endsWith(".djvu") && !file.name.lowercase().endsWith(".pdf")) {
                    com.google.android.material.snackbar.Snackbar.make(view, getString(R.string.ui_unsupported_format), com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setAction("Action", null).show()
                } else if (BooksCollection.getInstance(applicationContext).hasBook(file)) {
                    com.google.android.material.snackbar.Snackbar.make(view, getString(R.string.ui_book_already_added), com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setAction("Action", null).show()
                } else {
                    BookCreatorTask().execute(file)
                }
            } else if (!file.canRead()) {
                com.google.android.material.snackbar.Snackbar.make(view, getString(R.string.ui_no_permission), com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
            fileListAdapter.setRoot(i)
        }
    }

    inner class BookCreatorTask : AsyncTask<File, Void, Void>() {
        override fun doInBackground(vararg files: File): Void? {
            val file = files[0]
            applicationContext.startActivity(Intent("android.intent.action.VIEW", Uri.fromFile(file)).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
            })
            return null
        }
    }

    fun browseInternalMemory(view: View) {
        fileListAdapter.setRoot(Environment.getExternalStorageDirectory().absolutePath)
        fileListAdapter.notifyDataSetChanged()
    }
}
