package com.veve.flowreader.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.veve.flowreader.BuildConfig
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity() {

    companion object {
        const val FILE_OPEN_REQUEST = 42
    }

    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private lateinit var bookListAdapter: BookListAdapter
    private lateinit var bookGridAdapter: BookGridAdapter
    private var columnsNumber: Int = 0
    private lateinit var preferences: SharedPreferences
    private lateinit var borderPaint: Paint

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val bookId = intent.getLongExtra(Constants.BOOK_ID, 0)
        if (bookId > 0) {
            bookGridAdapter.removeBook(bookId)
            bookListAdapter.removeBook(bookId)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        bookListAdapter.refresh()
        bookGridAdapter.refresh()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.book_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        when (item.itemId) {
            R.id.delete_listed_book -> {
                val bookRecord = bookListAdapter.getItem(info.position) as BookRecord
                AlertDialog.Builder(this)
                    .setTitle(R.string.book_deletion)
                    .setMessage(String.format(resources.getString(R.string.confirm_delete), bookRecord.title))
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        BooksCollection.getInstance(applicationContext).deleteBook(bookRecord.id!!)
                        bookListAdapter.refresh()
                        bookGridAdapter.refresh()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
                    .create().show()
            }
            R.id.rename_listed_book -> {
                val bookRecord = bookListAdapter.getItem(info.position) as BookRecord
                val titleEditView = EditText(applicationContext).apply {
                    setText(bookRecord.title)
                    setTextColor(resources.getColor(R.color.colorPrimaryDark))
                }
                AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.rename_this_book))
                    .setMessage(R.string.new_title)
                    .setCancelable(false)
                    .setView(titleEditView)
                    .setPositiveButton(R.string.save) { _, _ ->
                        bookRecord.title = titleEditView.text.toString()
                        BooksCollection.getInstance(applicationContext).updateBook(bookRecord)
                        bookListAdapter.refresh()
                        bookGridAdapter.refresh()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .create().show()
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == FILE_OPEN_REQUEST) {
            try {
                startActivity(Intent(this, GetBookActivity::class.java).apply { this.data = data.data })
            } catch (e: Exception) {
                Log.v(javaClass.name, "Couldn't start GetBookActivity")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getPreferences(MODE_PRIVATE)
        bookListAdapter = BookListAdapter()
        bookGridAdapter = BookGridAdapter()
        requestPermissions()
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        (findViewById<ImageButton>(R.id.theme)).setImageResource(
            if (isNightMode()) R.drawable.ic_baseline_brightness_5_24
            else R.drawable.ic_baseline_brightness_3_24
        )

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivityForResult(
                Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/vnd.djvu"))
                }, "Choose the file to read.."),
                FILE_OPEN_REQUEST
            )
        }

        val gridView = findViewById<GridView>(R.id.grid)
        val hPad = resources.displayMetrics.widthPixels / 8
        gridView.setPaddingRelative(hPad, gridView.paddingTop, hPad, gridView.paddingBottom)

        if (!preferences.contains(Constants.VIEW_TYPE)) {
            preferences.edit().putInt(Constants.VIEW_TYPE, Constants.GRID_VIEW_TYPE).apply()
        }

        if (preferences.getInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE) == Constants.LIST_VIEW_TYPE) {
            gridView.numColumns = 1
            gridView.adapter = bookListAdapter
        } else {
            gridView.adapter = bookGridAdapter
        }

        gridView.setOnItemClickListener { parent, _, position, _ ->
            val selectedBook = parent.getItemAtPosition(position) as BookRecord
            startActivity(Intent(this, PageActivity::class.java).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Constants.FILE_NAME, selectedBook.url)
                putExtra(Constants.BOOK_ID, selectedBook.id)
            })
        }

        gridView.setOnItemLongClickListener { _, _, _, _ -> false }

        gridView.viewTreeObserver.addOnGlobalLayoutListener {
            val bookThumbPx = (Constants.BOOK_THUMB_WIDTH * Resources.getSystem().displayMetrics.density).toInt()
            val bookThumbPaddingPx = (Constants.BOOK_THUMB_HOR_PADDING * Resources.getSystem().displayMetrics.density).toInt()
            columnsNumber = gridView.width / (bookThumbPx + 2 * bookThumbPaddingPx)
            gridView.numColumns = if (preferences.getInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE) == Constants.LIST_VIEW_TYPE) 1 else columnsNumber
        }

        registerForContextMenu(gridView)

        findViewById<ImageButton>(R.id.books_list).setOnClickListener {
            gridView.numColumns = 1
            gridView.adapter = bookListAdapter
            preferences.edit().putInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE).apply()
        }

        findViewById<ImageButton>(R.id.books_grid).setOnClickListener {
            gridView.numColumns = columnsNumber
            gridView.adapter = bookGridAdapter
            preferences.edit().putInt(Constants.VIEW_TYPE, Constants.GRID_VIEW_TYPE).apply()
        }

        borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 1f
        }

        findViewById<View>(R.id.about_btn).setOnClickListener { showAboutDialog() }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setCancelable(false)
            .setIcon(R.drawable.ic_icon_flowbook_pink)
            .setMessage(String.format(resources.getString(R.string.program_info), BuildConfig.GitHash, sdf.format(Date())))
            .setView(layoutInflater.inflate(R.layout.copyright_text, null))
            .setNeutralButton(R.string.close) { dialog, _ -> dialog.cancel() }
            .create().apply { setCanceledOnTouchOutside(true) }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu) = false

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }

    override fun changeTheme(view: View) = changeTheme()

    inner class BookListAdapter : BaseAdapter() {
        private var booksList: List<BookRecord> = BooksCollection.getInstance(applicationContext).getBooks() ?: emptyList()

        init { notifyDataSetChanged() }

        fun removeBook(bookId: Long) {
            (booksList as? MutableList)?.removeIf { it.id == bookId }
            notifyDataSetChanged()
        }

        override fun getCount() = booksList.size
        override fun getItem(position: Int) = booksList[position]
        override fun getItemId(position: Int) = booksList[position].id ?: 0L

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.book_items_list, container, false)
            val bytes = booksList[position].preview!!
            var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (darkTheme) bmp = createInvertedBitmap(bmp)
            view.findViewById<ImageView>(R.id.thumbnail).setImageBitmap(bmp)
            view.findViewById<TextView>(R.id.text).text = booksList[position].title
            return view
        }

        override fun hashCode() = 1
        override fun equals(other: Any?) = other is BookListAdapter

        fun refresh() {
            booksList = BooksCollection.getInstance(applicationContext).getBooks() ?: emptyList()
            notifyDataSetChanged()
        }
    }

    inner class BookGridAdapter : BaseAdapter() {
        private var booksList: List<BookRecord> = BooksCollection.getInstance(applicationContext).getBooks() ?: emptyList()

        init { notifyDataSetChanged() }

        fun removeBook(bookId: Long) {
            (booksList as? MutableList)?.removeIf { it.id == bookId }
            notifyDataSetChanged()
        }

        override fun getCount() = booksList.size
        override fun getItem(position: Int) = booksList[position]
        override fun getItemId(position: Int) = booksList[position].id ?: 0L

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.book_preview, container, false)
            val bytes = booksList[position].preview!!
            var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (darkTheme) bmp = createInvertedBitmap(bmp)
            view.findViewById<ImageView>(R.id.thumbnail).setImageBitmap(bmp)
            view.findViewById<TextView>(R.id.caption).text = booksList[position].title
            return view
        }

        override fun hashCode() = 1
        override fun equals(other: Any?) = other is BookGridAdapter

        fun refresh() {
            booksList = BooksCollection.getInstance(applicationContext).getBooks() ?: emptyList()
            notifyDataSetChanged()
        }
    }
}
