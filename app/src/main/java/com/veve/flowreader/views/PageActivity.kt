package com.veve.flowreader.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.veve.flowreader.BuildConfig
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.PageRenderer
import com.veve.flowreader.model.PageRendererFactory
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class PageActivity : BaseActivity() {

    lateinit var kindleGestureDetector: GestureDetectorCompat
    lateinit var gestureDetectorCompat: GestureDetectorCompat
    lateinit var scaleGestureDetector: ScaleGestureDetector
    lateinit var pref: SharedPreferences
    var currentPage: Int = 0
    var zoomFactor: Float = 1.0f
    lateinit var runningTasks: MutableSet<AsyncTask<*, *, *>>
    lateinit var pager: TextView
    lateinit var progressPercent: TextView
    var glyphSizeLabel: TextView? = null
    lateinit var bar: AppBarLayout
    lateinit var topLayout: CoordinatorLayout
    var pageRenderer: PageRenderer? = null
    lateinit var book: BookRecord
    lateinit var seekBar: SeekBar
    lateinit var progressBar: com.google.android.material.progressindicator.CircularProgressIndicator
    lateinit var home: FloatingActionButton
    lateinit var show: FloatingActionButton
    lateinit var page: LinearLayout
    lateinit var context: DevicePageContext
    lateinit var scroll: ScrollView
    var viewMode: Int = 0
    lateinit var booksCollection: BooksCollection
    lateinit var bottomBar: LinearLayout
    var barsVisible: Boolean = false
    var pageLoader: PageLoader? = null

    override fun onPause() {
        super.onPause()
        if (!::book.isInitialized) {
            Log.i(javaClass.name, "The book is missing")
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            return
        }
        book.scrollOffset = scroll.scrollY
        booksCollection.updateBook(book)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        book.scrollOffset = scroll.scrollY
        booksCollection.updateBook(book)
        pageRenderer?.closeBook()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu) = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.v(javaClass.name, "${javaClass.name} onNewIntent# ${hashCode()}")
        Log.d("INTENT_ONNEWINTENT", "${getIntent().getLongExtra(Constants.BOOK_ID, 0)} = getIntent().getLongExtra(Constants.BOOK_ID, 0); hash = ${intent.hashCode()}")
        val position = getIntent().getIntExtra("position", 0)
        booksCollection = BooksCollection.getInstance(applicationContext)
        book = booksCollection.getBook(position.toLong()) ?: return
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        booksCollection.updateBook(book)
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scroll.smoothScrollBy(distanceX.toInt(), distanceY.toInt())
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            Log.d(javaClass.name, "On Fling")
            val distanceX = e2.rawX - (e1?.rawX ?: e2.rawX)
            val distanceY = e2.rawY - (e1?.rawY ?: e2.rawY)
            if (Math.abs(distanceX) > 2 * Math.abs(distanceY) && Math.abs(distanceX) > 50) {
                if (distanceX < 0) {
                    if (book.currentPage!! < book.pagesCount!! - 1) {
                        setPageNumber(book.currentPage!! + 1)
                        scroll.scrollTo(0, 0)
                    }
                } else {
                    if (book.currentPage!! > 0) {
                        setPageNumber(book.currentPage!! - 1)
                        scroll.scrollTo(0, 0)
                    }
                }
            } else {
                scroll.fling(-velocityY.toInt())
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (barsVisible) {
                bottomBar.visibility = View.INVISIBLE
                bar.visibility = View.GONE
                barsVisible = false
            } else {
                bottomBar.visibility = View.VISIBLE
                bar.visibility = View.VISIBLE
                barsVisible = true
            }
            return super.onSingleTapConfirmed(e)
        }
    }

    inner class KindleGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scroll.smoothScrollBy(distanceX.toInt(), distanceY.toInt())
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            scroll.fling(-velocityY.toInt())
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            val screenWidth = window.decorView.width
            val screenHeight = window.decorView.height
            val cellWidth = screenWidth / 3f
            val cellHeight = screenHeight / 3f
            val col = (x / cellWidth).toInt()
            val row = (y / cellHeight).toInt()

            if (row == 1 && col == 1) {
                if (barsVisible) {
                    bottomBar.visibility = View.INVISIBLE
                    bar.visibility = View.GONE
                    barsVisible = false
                } else {
                    bottomBar.visibility = View.VISIBLE
                    bar.visibility = View.VISIBLE
                    barsVisible = true
                }
            } else {
                if (col == 2) {
                    if (book.currentPage!! < book.pagesCount!! - 1) {
                        setPageNumber(book.currentPage!! + 1)
                        scroll.scrollTo(0, 0)
                    }
                } else if (col == 0) {
                    if (book.currentPage!! > 0) {
                        setPageNumber(book.currentPage!! - 1)
                        scroll.scrollTo(0, 0)
                    }
                }
            }
            return true
        }
    }

    inner class MyOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var factor = 1.0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            Log.v(javaClass.name, "onScale")
            factor *= detector.scaleFactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            factor = 1.0f
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            zoomFactor = Math.abs(factor * context.zoom)
            zoomFactor = Math.min(zoomFactor, context.zoomMax)
            zoomFactor = Math.max(zoomFactor, context.zoomMin)
            Log.d(javaClass.name, "Scaling $factor zoom $zoomFactor")
            context.zoom = zoomFactor
            book.zoom = zoomFactor
            updateGlyphSizeLabel()
            Log.d(javaClass.name, "Scaling factor is ${book.zoom} original is ${book.zoomOriginal}")
            setPageNumber(book.currentPage!!)
            Log.d(javaClass.name, "Scale ended")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (pref.getBoolean(Constants.KINDLE_NAVIGATION, false)) {
            kindleGestureDetector.onTouchEvent(event)
            true
        } else {
            val flingProcessed = gestureDetectorCompat.onTouchEvent(event)
            val pinchProcessed = if (pref.getBoolean(Constants.PINCH_ZOOM, true)) {
                scaleGestureDetector.onTouchEvent(event)
            } else false
            flingProcessed || pinchProcessed
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = applicationContext.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)
        Log.v(javaClass.name, "${javaClass.name} onCreate# ${hashCode()}")
        runningTasks = CopyOnWriteArraySet()
        setContentView(R.layout.activity_page)
        gestureDetectorCompat = GestureDetectorCompat(this, MyGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, MyOnScaleGestureListener())
        kindleGestureDetector = GestureDetectorCompat(this, KindleGestureListener())
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        val bookId = intent.getLongExtra(Constants.BOOK_ID, 0)
        Log.d("INTENT_ONCREATE", "$bookId = getIntent().getLongExtra(${Constants.BOOK_ID}, 0); hash = ${intent.hashCode()}")
        booksCollection = BooksCollection.getInstance(applicationContext)
        val bookOrNull = booksCollection.getBook(bookId)
        Log.d("INTENT_ONCREATE", "$bookOrNull = booksCollection.getBook($bookId);")

        if (bookOrNull == null) {
            Log.i(javaClass.name, "The book with id $bookId is missing")
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            return
        }
        book = bookOrNull

        try {
            pageRenderer = PageRendererFactory.getRenderer(booksCollection, book)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        currentPage = book.currentPage!!
        viewMode = book.mode

        bar = findViewById(R.id.bar)
        topLayout = findViewById(R.id.topLayout)
        pager = findViewById(R.id.pager)
        progressPercent = findViewById(R.id.progress_percent)
        glyphSizeLabel = if (BuildConfig.DEBUG) findViewById(R.id.glyph_size_label) else null
        seekBar = findViewById(R.id.slider)
        home = findViewById(R.id.home)
        progressBar = findViewById(R.id.progress)
        page = findViewById(R.id.page)
        show = findViewById(R.id.show)
        scroll = findViewById(R.id.scroll)
        bottomBar = findViewById(R.id.bottomBar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<View>(R.id.page).setOnGenericMotionListener { _, _ -> false }

        seekBar.max = book.pagesCount!!
        pager.setOnTouchListener(PagerTouchListener())
        seekBar.setOnSeekBarChangeListener(PagerListener())
        home.setOnClickListener(HomeButtonListener())
        show.setOnClickListener(ShowListener())
        topLayout.addOnLayoutChangeListener(LayoutListener())

        @Suppress("DEPRECATION")
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        context = DevicePageContext(point.x)

        val displayMetrics = resources.displayMetrics
        context.resolution = displayMetrics.xdpi.toInt()
        context.setZoomLimits(book.medianGlyphBaseHeight, displayMetrics.xdpi)

        var storedZoom = book.zoom
        if (storedZoom < context.zoomMin || storedZoom > context.zoomMax) {
            book.medianGlyphBaseHeight = 0f
            booksCollection.deleteAllGlyphs(book.id!!)
            booksCollection.updateBook(book)
            context.setZoomLimits(0f, displayMetrics.xdpi)
            storedZoom = context.zoomMin
        }
        context.zoom = storedZoom
        book.zoom = context.zoom
        context.zoomOriginal = Math.min(Math.max(book.zoomOriginal, context.zoomMin), 1.0f)
        book.zoomOriginal = context.zoomOriginal
        context.kerning = book.kerning
        context.leading = book.leading
        context.margin = book.margin
        context.breakWords = book.breakWords
        if (book.preprocessing) {
            context.preprocessing = true
            context.invalidateCache = true
        } else {
            context.preprocessing = false
            context.invalidateCache = false
        }

        setPageNumber(currentPage)
        book.zoom = context.zoom
        show.setImageResource(if (viewMode == Constants.VIEW_MODE_PHONE) R.drawable.ic_to_book else R.drawable.ic_to_phone)

        findViewById<View>(R.id.scroll).setOnTouchListener { _, event -> onTouchEvent(event) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        context.invalidateCache = false
        when (item.itemId) {
            R.id.navigation -> {
                val kindle = pref.getBoolean(Constants.KINDLE_NAVIGATION, false)
                pref.edit().putBoolean(Constants.KINDLE_NAVIGATION, !kindle).apply()
                item.setTitle(if (kindle) R.string.kindle_navigation else R.string.ipad_navigation)
            }
            R.id.night_mode -> {
                changeTheme()
                return true
            }
            R.id.settings -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
                return true
            }
        }
        setPageNumber(currentPage)
        return true
    }

    private fun showSettingsPopup(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_page_menu, null)
        val kindleNav = pref.getBoolean(Constants.KINDLE_NAVIGATION, false)
        val pinchZoom = pref.getBoolean(Constants.PINCH_ZOOM, true)
        val breakWords = book.breakWords

        popupView.findViewById<View>(R.id.check_swipe).visibility = if (kindleNav) View.GONE else View.VISIBLE
        popupView.findViewById<View>(R.id.check_tap).visibility = if (kindleNav) View.VISIBLE else View.GONE

        val switchPinch = popupView.findViewById<CompoundButton>(R.id.switch_pinch)
        switchPinch.isChecked = pinchZoom
        switchPinch.setOnCheckedChangeListener { _, checked ->
            pref.edit().putBoolean(Constants.PINCH_ZOOM, checked).apply()
        }

        val switchBreakOnSpace = popupView.findViewById<CompoundButton>(R.id.switch_break_on_space)
        switchBreakOnSpace.isChecked = breakWords

        val dialog = AppCompatDialog(this)
        dialog.setContentView(popupView)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.3f)
        dialog.window?.setLayout(
            (300 * resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        switchBreakOnSpace.setOnCheckedChangeListener { _, checked ->
            book.breakWords = checked
            context.breakWords = checked
            booksCollection.updateBook(book)
            context.invalidateCache = true
            dialog.dismiss()
            setPageNumber(currentPage)
        }

        if (BuildConfig.DEBUG) {
            val rowGlyphBorders = popupView.findViewById<View>(R.id.row_glyph_borders)
            rowGlyphBorders.visibility = View.VISIBLE
            val switchGlyphBorders = popupView.findViewById<CompoundButton>(R.id.switch_glyph_borders)
            switchGlyphBorders.isChecked = context.isShowGlyphBorders()
            switchGlyphBorders.setOnCheckedChangeListener { _, checked ->
                context.showGlyphBorders = checked
                dialog.dismiss()
                setPageNumber(currentPage)
            }
        }

        popupView.findViewById<View>(R.id.option_swipe).setOnClickListener {
            pref.edit().putBoolean(Constants.KINDLE_NAVIGATION, false).apply()
            dialog.dismiss()
        }
        popupView.findViewById<View>(R.id.option_tap).setOnClickListener {
            pref.edit().putBoolean(Constants.KINDLE_NAVIGATION, true).apply()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun setPageNumber(pageNumber: Int) {
        pager.text = getString(R.string.ui_page_count, pageNumber + 1, book.pagesCount!!)
        seekBar.progress = pageNumber + 1
        currentPage = pageNumber
        book.currentPage = pageNumber
        booksCollection.updateBook(book)
        val loader = PageLoader(this)
        pageLoader = loader
        kickOthers(loader)
        loader.execute(pageNumber, if (context.isInvalidateCache()) 1 else 0)
    }

    private fun kickOthers(loader: PageLoader) {
        for (task in runningTasks) {
            if (!task.isCancelled) {
                task.cancel(true)
                Log.d(javaClass.name, "Cancelling task #${task.hashCode()}")
            }
        }
        runningTasks.add(loader)
        Log.d(javaClass.name, "Adding task #${loader.hashCode()}")
    }

    // ////////////////////// LISTENERS ///////////////////////////////////////////////////

    inner class PagerTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            view.performClick()
            pager.visibility = View.GONE
            seekBar.visibility = View.VISIBLE
            return true
        }
    }

    inner class PagerListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            Log.d(javaClass.name, "onProgressChanged. $progress %%")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            Log.d(javaClass.name, "onStartTrackingTouch")
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            Log.d(javaClass.name, "onStopTrackingTouch")
            val progress = seekBar.progress
            setPageNumber(if (progress > 0) progress - 1 else progress)
            seekBar.visibility = View.GONE
            pager.visibility = View.VISIBLE
        }
    }

    inner class HomeButtonListener : View.OnClickListener {
        override fun onClick(view: View) {
            startActivity(Intent(this@PageActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    inner class LayoutListener : View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int,
                                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            if (pageRenderer == null) {
                try {
                    pageRenderer = PageRendererFactory.getRenderer(booksCollection, book)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val pageMenuListener = PageMenuListener()
            findViewById<View>(R.id.smaller_text).setOnClickListener(pageMenuListener)
            findViewById<View>(R.id.larger_text).setOnClickListener(pageMenuListener)
            findViewById<View>(R.id.page_settings).setOnClickListener { btn -> showSettingsPopup(btn) }
            scroll.scrollTo(0, book.scrollOffset)
        }
    }

    inner class ShowListener : View.OnClickListener {
        override fun onClick(view: View) {
            if (viewMode == Constants.VIEW_MODE_ORIGINAL) {
                viewMode = Constants.VIEW_MODE_PHONE
                book.mode = Constants.VIEW_MODE_PHONE
                val res = applicationContext.resources.getDrawable(R.drawable.ic_to_book, null)
                Log.d(javaClass.name, "resource state is ${res.constantState?.hashCode()}")
                show.setImageDrawable(res)
                Log.d(javaClass.name, "button resource state is now ${show.drawable.constantState?.hashCode()}")
                Snackbar.make(view, getString(R.string.ui_reflow_page), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                Log.d(javaClass.name, "Setting page #$currentPage for modified page")
            } else if (viewMode == Constants.VIEW_MODE_PHONE) {
                viewMode = Constants.VIEW_MODE_ORIGINAL
                book.mode = Constants.VIEW_MODE_ORIGINAL
                show.setImageResource(R.drawable.ic_to_phone)
                Snackbar.make(view, getString(R.string.ui_original_page), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                Log.d(javaClass.name, "Setting page #$currentPage for original page")
            }
            setPageNumber(currentPage)
        }
    }

    inner class PageMenuListener : View.OnClickListener {
        override fun onClick(v: View) {
            val id = v.id
            if (viewMode == Constants.VIEW_MODE_PHONE) {
                if (id == R.id.smaller_text) {
                    if (context.zoom > context.zoomMin) {
                        context.zoom = Math.max(context.zoom - context.zoomStep, context.zoomMin)
                        book.zoom = context.zoom
                    }
                } else if (id == R.id.larger_text) {
                    if (context.zoom < context.zoomMax) {
                        context.zoom = Math.min(context.zoom + context.zoomStep, context.zoomMax)
                        book.zoom = context.zoom
                    }
                }
                updateGlyphSizeLabel()
                setPageNumber(currentPage)
            } else {
                var changed = false
                if (id == R.id.smaller_text) {
                    if (context.zoomOriginal > context.zoomMin) {
                        context.zoomOriginal = Math.max(context.zoomOriginal - context.zoomStep, context.zoomMin)
                        book.zoomOriginal = context.zoomOriginal
                        changed = true
                    }
                } else if (id == R.id.larger_text) {
                    if (context.zoomOriginal < 1.0f) {
                        context.zoomOriginal = Math.min(context.zoomOriginal + context.zoomStep, 1.0f)
                        book.zoomOriginal = context.zoomOriginal
                        changed = true
                    }
                }
                if (changed) {
                    booksCollection.updateBook(book)
                    rescaleOriginalPage()
                }
            }
        }
    }

    fun updateGlyphSizeLabel() {
        val label = glyphSizeLabel ?: return
        val baseHeight = book.medianGlyphBaseHeight
        if (baseHeight > 0) {
            val px = Math.round(baseHeight * context.zoom)
            val dpi = if (context.resolution > 0) context.resolution.toFloat() else 72f
            val pt = Math.round(baseHeight * context.zoom * 72f / dpi)
            label.text = "${px}px / ${pt}pt"
        } else {
            label.text = ""
        }
    }

    fun rescaleOriginalPage() {
        setPageNumber(currentPage)
    }

    // ////////////////////// ASYNC TASKS /////////////////////////////////////////////////

    inner class PageLoader(context: PageActivity) : AsyncTask<Int, Void, Void>() {

        var bitmaps: List<Bitmap> = emptyList()
        val pageActivity: PageActivity
        private val pageActivityReference = WeakReference(context)

        init {
            pageActivity = pageActivityReference.get()!!
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            pageActivity.runningTasks.remove(this)
        }

        override fun onCancelled() {
            super.onCancelled()
            pageActivity.runningTasks.remove(this)
        }

        override fun doInBackground(vararg integers: Int?): Void? {
            runOnUiThread {
                pageActivity.scroll.visibility = View.INVISIBLE
                findViewById<View>(R.id.progress_container).visibility = View.VISIBLE
            }
            val pageNumber = integers[0]!!
            if (pageActivity.viewMode == Constants.VIEW_MODE_PHONE) {
                bitmaps = CopyOnWriteArrayList(pageActivity.pageRenderer!!.renderPage(context, pageNumber))
                Log.v(javaClass.name, "Get ${bitmaps.size} bitmaps for page $pageNumber")
            } else {
                bitmaps = listOf(pageActivity.pageRenderer!!.renderOriginalPage(pageNumber))
            }
            runOnUiThread {
                val pageViews = mutableListOf<View>()
                for (bitmap in bitmaps) {
                    (pageActivity.page.parent as? ViewGroup)?.removeView(pageActivity.page)
                    pageActivity.scroll.removeAllViews()
                    if (viewMode == Constants.VIEW_MODE_PHONE) {
                        val srcWidth = bitmap.width
                        val srcHeight = bitmap.height
                        val dstWidth = context.width
                        val scale = dstWidth.toFloat() / srcWidth
                        val dstHeight = (srcHeight * scale).toInt()
                        val lightFilter: ColorFilter? = if (darkTheme) null else ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                            243f/255f, 0f, 0f, 0f, 0f,
                            0f, 243f/255f, 0f, 0f, 0f,
                            0f, 0f, 243f/255f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )))
                        var dstOffset = 0
                        while (dstOffset < dstHeight) {
                            val dstChunkHeight = Math.min(Constants.IMAGE_VIEW_HEIGHT_LIMIT, dstHeight - dstOffset)
                            val srcOffset = (dstOffset / scale).toInt()
                            val srcEnd = Math.min(Math.ceil((dstOffset + dstChunkHeight) / scale.toDouble()).toInt(), srcHeight)
                            val srcChunkHeight = srcEnd - srcOffset
                            if (srcChunkHeight <= 0) break
                            var scaledChunk = Bitmap.createScaledBitmap(
                                Bitmap.createBitmap(bitmap, 0, srcOffset, srcWidth, srcChunkHeight),
                                dstWidth, dstChunkHeight, true
                            )
                            if (darkTheme) {
                                val inverted = createInvertedBitmap(scaledChunk)
                                scaledChunk.recycle()
                                scaledChunk = inverted
                            }
                            val imageView = ImageView(applicationContext).apply {
                                scaleType = ImageView.ScaleType.FIT_START
                                adjustViewBounds = true
                                maxHeight = Int.MAX_VALUE
                                setImageBitmap(scaledChunk)
                                colorFilter = lightFilter
                            }
                            pageViews.add(imageView)
                            dstOffset += Constants.IMAGE_VIEW_HEIGHT_LIMIT
                        }
                        if (!bitmap.isRecycled) bitmap.recycle()
                        pageActivity.page.removeAllViews()
                        for (view in pageViews) pageActivity.page.addView(view)
                        pageActivity.scroll.addView(pageActivity.page)
                    } else {
                        inviteToTryReflow(bitmap)
                        val srcWidth = bitmap.width
                        val srcHeight = bitmap.height
                        val dstWidth = (context.zoomOriginal * context.width).toInt()
                        val scale = dstWidth.toFloat() / srcWidth
                        val dstHeight = (srcHeight * scale).toInt()
                        val lightFilterOrig: ColorFilter? = if (darkTheme) null else ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                            243f/255f, 0f, 0f, 0f, 0f,
                            0f, 243f/255f, 0f, 0f, 0f,
                            0f, 0f, 243f/255f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )))
                        val origChunkLimit = Math.min(Constants.IMAGE_VIEW_HEIGHT_LIMIT,
                            Constants.MAX_BITMAP_SIZE / (dstWidth * 4))
                        Log.v(javaClass.name, "Original page: src=${srcWidth}x${srcHeight} dst=${dstWidth}x${dstHeight} chunkLimit=$origChunkLimit zoomOriginal=${context.zoomOriginal} screenW=${context.width}")
                        pageActivity.page.removeAllViewsInLayout()
                        val horizontalScrollView = HorizontalScrollView(applicationContext)
                        var dstOffset = 0
                        while (dstOffset < dstHeight) {
                            val dstChunkHeight = Math.min(origChunkLimit, dstHeight - dstOffset)
                            val srcOffset = (dstOffset / scale).toInt()
                            val srcEnd = Math.min(Math.ceil((dstOffset + dstChunkHeight) / scale.toDouble()).toInt(), srcHeight)
                            val srcChunkHeight = srcEnd - srcOffset
                            if (srcChunkHeight <= 0) break
                            var scaledChunk = Bitmap.createScaledBitmap(
                                Bitmap.createBitmap(bitmap, 0, srcOffset, srcWidth, srcChunkHeight),
                                dstWidth, dstChunkHeight, true
                            )
                            if (darkTheme) {
                                val inverted = createInvertedBitmap(scaledChunk)
                                scaledChunk.recycle()
                                scaledChunk = inverted
                            }
                            val imageView = ImageView(applicationContext).apply {
                                layoutParams = LinearLayout.LayoutParams(dstWidth, dstChunkHeight)
                                scaleType = ImageView.ScaleType.FIT_XY
                                setImageBitmap(scaledChunk)
                                colorFilter = lightFilterOrig
                            }
                            pageViews.add(imageView)
                            dstOffset += dstChunkHeight
                        }
                        if (!bitmap.isRecycled) bitmap.recycle()
                        for (view in pageViews) pageActivity.page.addView(view)
                        horizontalScrollView.addView(pageActivity.page)
                        pageActivity.scroll.addView(horizontalScrollView)
                        horizontalScrollView.setOnTouchListener { _, event ->
                            this@PageActivity.onTouchEvent(event)
                            false
                        }
                    }
                }
                pageActivity.scroll.visibility = View.VISIBLE
                findViewById<View>(R.id.progress_container).visibility = View.INVISIBLE
                pageActivity.scroll.scrollTo(0, 0)
                pageActivity.updateGlyphSizeLabel()
            }
            return null
        }

        private fun inviteToTryReflow(bitmap: Bitmap) {
            if (bitmap.width <= context.width) return
            if (pref.contains(Constants.SHOW_TRY_REFLOW)) return
            pref.edit().putBoolean(Constants.SHOW_TRY_REFLOW, false).apply()
            AlertDialog.Builder(this@PageActivity)
                .setTitle(resources.getString(R.string.try_reflow))
                .setMessage(R.string.try_reflow_explained)
                .setCancelable(true)
                .setIcon(R.drawable.ic_to_phone_large)
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
                .setNegativeButton(R.string.ok_not_anymore) { dialog, _ -> dialog.cancel() }
                .create().show()
        }
    }
}
