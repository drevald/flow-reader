package com.veve.flowreader.views;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.veve.flowreader.Constants.BOOK_CONTEXT;
import static com.veve.flowreader.Constants.BOOK_ID;
import static com.veve.flowreader.Constants.KINDLE_NAVIGATION;
import static com.veve.flowreader.Constants.MAX_BITMAP_SIZE;
import static com.veve.flowreader.Constants.POSITION;
import static com.veve.flowreader.Constants.PREFERENCES;
import static com.veve.flowreader.Constants.REPORT_ID;
import static com.veve.flowreader.Constants.REPORT_URL;
import static com.veve.flowreader.Constants.SHOW_SCROLLBARS;
import static com.veve.flowreader.Constants.VIEW_MODE_ORIGINAL;
import static com.veve.flowreader.Constants.VIEW_MODE_PHONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GestureDetectorCompat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.DaoAccess;
import com.veve.flowreader.dao.ReportRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Designed to show a book page with page controls
 */
public class PageActivity extends BaseActivity {

    GestureDetectorCompat kindleGestureDetector;
    GestureDetectorCompat gestureDetectorCompat;
    ScaleGestureDetector scaleGestureDetector;

    SharedPreferences pref;

    public int currentPage;
    public float zoomFactor = 1;

    Set<AsyncTask> runningTasks;
    TextView pager;
    AppBarLayout bar;
    CoordinatorLayout topLayout;
    PageRenderer pageRenderer;
    BookRecord book;
    SeekBar seekBar;
    ProgressBar progressBar;
    FloatingActionButton home;
    FloatingActionButton show;
    LinearLayout page;
    DevicePageContext context;
    PageActivity pageActivity;
    ScrollView scroll;
    int viewMode;
    BooksCollection booksCollection;
    LinearLayout bottomBar;
    boolean barsVisible;
    String commitId = "$Id$";
    PageLoader pageLoader;

    @Override
    protected void onPause() {
        super.onPause();
        if (book == null) {
            Log.i(getClass().getName(), "The book is missing");
            Intent i = new Intent(PageActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            return;
        }
        book.setScrollOffset(scroll.getScrollY());
        booksCollection.updateBook(book);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        book.setScrollOffset(scroll.getScrollY());
        booksCollection.updateBook(book);
        pageRenderer.closeBook();
        Intent i = new Intent(PageActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        finish();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.page_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
            m.getItem(4).setIcon(book.getPreprocessing()
                    ? R.drawable.ic_unenhance : R.drawable.ic_enhance);
            m.getItem(4).setTitle(book.getPreprocessing()
                    ? R.string.unenhance : R.string.enhance);
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(getClass().getName(), getClass().getName() + "onNewIntent# " + this.hashCode());
        Log.d("INTENT_ONNEWINTENT",  getIntent().getLongExtra(BOOK_ID, 0)
                + " = getIntent().getLongExtra(Constants.BOOK_ID, 0); hash = " + intent.hashCode());
        int position = getIntent().getIntExtra("position", 0);
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        booksCollection.updateBook(book);
    }

    public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scroll.smoothScrollBy((int)distanceX, (int)distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(getClass().getName(), "On Fling");
            float distanceX = e2.getRawX() - e1.getRawX();
            float distanceY = e2.getRawY() - e1.getRawY();
            if (Math.abs(distanceX) > 2 * Math.abs(distanceY) && Math.abs(distanceX) > 50) {
                if (distanceX < 0) {
                    if (book.getCurrentPage() < book.getPagesCount()-1) {
                        setPageNumber(book.getCurrentPage()+1);
                        scroll.scrollTo(0, 0);
                    }
                } else {
                    if (book.getCurrentPage() > 0) {
                        setPageNumber(book.getCurrentPage()-1);
                        scroll.scrollTo(0, 0);
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (barsVisible) {
                bottomBar.setVisibility(INVISIBLE);
                bar.setVisibility(GONE);
                barsVisible = false;
            } else {
                bottomBar.setVisibility(VISIBLE);
                bar.setVisibility(VISIBLE);
                barsVisible = true;
            }
            return super.onSingleTapConfirmed(e);
        }

    }

    public class KindleGestureListener extends  GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
            scroll.smoothScrollBy((int)distanceX, (int)distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            if (y < getWindow().getDecorView().getHeight()/6) {
                if (barsVisible) {
                    bottomBar.setVisibility(INVISIBLE);
                    bar.setVisibility(GONE);
                    barsVisible = false;
                } else {
                    bottomBar.setVisibility(VISIBLE);
                    bar.setVisibility(VISIBLE);
                    barsVisible = true;
                }
            } else {
                if (x > getWindow().getDecorView().getWidth() / 3) {
                    if (book.getCurrentPage() < book.getPagesCount() - 1) {
                        setPageNumber(book.getCurrentPage() + 1);
                        scroll.scrollTo(0, 0);
                    }
                } else {
                    if (book.getCurrentPage() > 0) {
                        setPageNumber(book.getCurrentPage() - 1);
                        scroll.scrollTo(0, 0);
                    }
                }
            }
            return true;
        }

    }

    public class MyOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        float factor = 1.0f;

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.v(getClass().getName(), "onScale");
            factor *= detector.getScaleFactor();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            factor = 1.0f;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
//            super.onScaleEnd(detector);

            zoomFactor = Math.abs(factor * context.getZoom());
            zoomFactor = Math.min(zoomFactor, Constants.ZOOM_MAX);
            zoomFactor = Math.max(zoomFactor, Constants.ZOOM_MIN);
            Log.d(getClass().getName(),
                    String.format("Scaling %f zoom %f\n", factor, zoomFactor));
            context.setZoom(zoomFactor);
            book.setZoom(zoomFactor);
            Log.d(getClass().getName(),
                    String.format("Scaling factor is %f original is %f",
                            book.getZoom(), book.getZoomOriginal()));
            setPageNumber(book.getCurrentPage());

            Log.d(getClass().getName(), "Scale ended");
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pref.getBoolean(Constants.KINDLE_NAVIGATION, false)) {
            kindleGestureDetector.onTouchEvent(event);
            return true;
        } else {
            boolean flingProcessed = gestureDetectorCompat.onTouchEvent(event);
            boolean pinchProcessed = scaleGestureDetector.onTouchEvent(event);
            return flingProcessed || pinchProcessed;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        Log.v(getClass().getName(), getClass().getName() + "onCreate# " + this.hashCode());
        runningTasks = new CopyOnWriteArraySet<>();
        setContentView(R.layout.activity_page);
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new MyOnScaleGestureListener());
        kindleGestureDetector = new GestureDetectorCompat(this, new KindleGestureListener());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        long bookId = getIntent().getLongExtra(BOOK_ID, 0);
        Log.d("INTENT_ONCREATE", bookId + " = getIntent().getLongExtra(" + Constants.BOOK_ID + ", 0); hash = " + getIntent().hashCode());
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(bookId);
        Log.d("INTENT_ONCREATE", book + " = booksCollection.getBook(" + bookId + ");");

        if (book == null) {
            Log.i(getClass().getName(), String.format("The book with id %d is missing", bookId));
            Intent i = new Intent(PageActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            return;
        }

        try {
            pageRenderer = PageRendererFactory.getRenderer(booksCollection, book);
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentPage = book.getCurrentPage();

        viewMode = book.getMode();

        bar = findViewById(R.id.bar);
        topLayout = findViewById(R.id.topLayout);
        pager = findViewById(R.id.pager);
        seekBar = findViewById(R.id.slider);
        home = findViewById(R.id.home);
        progressBar = findViewById(R.id.progress);
        page = findViewById(R.id.page);
        show = findViewById(R.id.show);
        scroll = findViewById(R.id.scroll);
        bottomBar = findViewById(R.id.bottomBar);

        findViewById(R.id.help).setOnClickListener((view)->{
            Intent intent = new Intent(PageActivity.this, HelpActivity.class);
            intent.putExtra(BOOK_ID, book.getId());
            startActivity(intent);
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        findViewById(R.id.page).setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                return false;
            }
        });

        seekBar.setMax(book.getPagesCount());
        pager.setOnTouchListener(new PagerTouchListener());
        seekBar.setOnSeekBarChangeListener(new PagerListener());
        home.setOnClickListener(new HomeButtonListener());
        show.setOnClickListener(new ShowListener());
        topLayout.addOnLayoutChangeListener(new LayoutListener());

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        context = new DevicePageContext(point.x);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        context.setResolution((int)displayMetrics.xdpi);

        context.setZoom(book.getZoom());
        context.setZoomOriginal(book.getZoomOriginal());
        context.setKerning(book.getKerning());
        context.setLeading(book.getLeading());
        context.setMargin(book.getMargin());
        if (book.getPreprocessing()) {
            context.setPreprocessing(true);
            context.setInvalidateCache(true);
        } else {
            context.setPreprocessing(false);
            context.setInvalidateCache(false);
        }

//        TextView bookTitle = findViewById(R.id.book_title);
//        bookTitle.setText(book.getTitle());
//        bookTitle.setOnClickListener((view)->{
//            AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
//            builder.setCancelable(false)
//                    .setMessage(book.getTitle())
//                    .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//            AlertDialog alert = builder.create();
//            alert.show();
//        });

        pageActivity = this;
        setPageNumber(currentPage);

        book.setZoom(context.getZoom());
        show.setImageResource(viewMode == VIEW_MODE_PHONE ? R.drawable.ic_baseline_menu_book_24 : R.drawable.ic_baseline_smartphone_24);

        findViewById(R.id.scroll).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PageActivity.this.onTouchEvent(event);
            }
        });

    }

    public BookRecord getBook() {
        return book;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.preprocess);
        item.setIcon(book.getPreprocessing() ? R.drawable.ic_unenhance : R.drawable.ic_enhance);
        item.setTitle(book.getPreprocessing() ? R.string.unenhance : R.string.enhance );
        item = menu.findItem(R.id.navigation);
        item.setTitle(pref.getBoolean(KINDLE_NAVIGATION, false) ? R.string.ipad_navigation : R.string.kindle_navigation);
        item = menu.findItem(R.id.scrollbars);
        item.setIcon(pref.getBoolean(SHOW_SCROLLBARS, false) ? R.drawable.ic_noscrollbars : R.drawable.ic_scrollbars);
        item.setTitle(pref.getBoolean(SHOW_SCROLLBARS, false) ? R.string.hide_scrollbars : R.string.show_scrollbars );q
        if (viewMode == VIEW_MODE_PHONE) {
            menu.findItem(R.id.no_margins).setEnabled(true);
            menu.findItem(R.id.normal_margins).setEnabled(true);
            menu.findItem(R.id.wide_margins).setEnabled(true);
            menu.findItem(R.id.preprocess).setEnabled(true);
            menu.findItem(R.id.page_unreadable).setEnabled(true);
            menu.findItem(R.id.print).setEnabled(true);
        }
        if (viewMode == VIEW_MODE_ORIGINAL) {
            menu.findItem(R.id.no_margins).setEnabled(false);
            menu.findItem(R.id.normal_margins).setEnabled(false);
            menu.findItem(R.id.wide_margins).setEnabled(false);
            menu.findItem(R.id.preprocess).setEnabled(false);
            menu.findItem(R.id.page_unreadable).setEnabled(false);
            menu.findItem(R.id.print).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        context.setInvalidateCache(false);
        switch (item.getItemId()) {
            case R.id.no_margins: {
                context.setMargin(0.2f);
                Log.v(getClass().getName(), "Margin set to " + context.getMargin());
                book.setMargin(context.getMargin());
                break;
            }
            case R.id.normal_margins: {
                context.setMargin(1.0f);
                Log.v(getClass().getName(), "Margin set to " + context.getMargin());
                book.setMargin(context.getMargin());
                break;
            }
            case R.id.wide_margins: {
                context.setMargin(1.5f);
                Log.v(getClass().getName(), "Margin set to " + context.getMargin());
                book.setMargin(context.getMargin());
                break;
            }
            case R.id.page_unreadable: {
                ConnectionCheckerTask connectionCheckerTask = new ConnectionCheckerTask();
                connectionCheckerTask.execute();
                try {
                    if (!connectionCheckerTask.get()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
                        builder.setTitle(R.string.no_connection);
                        builder.setMessage(R.string.no_connection_explained);
                        builder.setNeutralButton(R.string.ok, (dialog, which)->{
                            dialog.dismiss();
                        });
                        builder.create().show();
                        break;
                    }
                } catch (Exception e)  {
                    e.printStackTrace();
                }

                Log.v("NULLBOOK", "Getting original page " + currentPage);
                Bitmap originalBitmap = pageRenderer.renderOriginalPage(currentPage);
                Log.v("NULLBOOK", "Original page " + currentPage + " is " + originalBitmap);
                List<Bitmap> reflowedBitmaps = pageLoader.bitmaps;
                Log.v("NULLBOOK", "Reflowed pages are " + reflowedBitmaps);
                ByteArrayOutputStream osOriginal = new ByteArrayOutputStream();
                ByteArrayOutputStream osReflowed = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, osOriginal);

                // only send the first bitmap, otherwise need to join bitmaps, risking OutOfMemory
                Bitmap reflowedBitmap = reflowedBitmaps.get(0);
                reflowedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, osReflowed);

                ObjectMapper mapper = new ObjectMapper(); // create once, reuse
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                File origBmpFile = null;
                File reflowedBmpFile = null;
                try {
                    origBmpFile = File.createTempFile(book.getId() + "_orig", null);
                    FileOutputStream origBmpFileOut = new FileOutputStream(origBmpFile);
                    origBmpFileOut.write(osOriginal.toByteArray());
                    origBmpFileOut.close();
                    origBmpFile.deleteOnExit();
                    Log.v(getClass().getName(), "Original bitmap stored in tmp file " + origBmpFile.getPath());

                    reflowedBmpFile = File.createTempFile(book.getId() + "_reflow", null);
                    FileOutputStream reflowedBmpFileOut = new FileOutputStream(reflowedBmpFile);
                    reflowedBmpFileOut.write(osReflowed.toByteArray());
                    reflowedBmpFileOut.close();
                    reflowedBmpFile.deleteOnExit();
                    Log.v(getClass().getName(), "Original bitmap stored in tmp file " + origBmpFile.getPath());
                    mapper.writeValue(baos, booksCollection.getPageGlyphs(book.getId(), currentPage, true));
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Failed to convert Glyphs to JSON", e);
                }
                ReportRecord reportRecord = new ReportRecord(
                        baos.toByteArray(),
                        origBmpFile.getPath().getBytes(),
                        reflowedBmpFile.getPath().getBytes());
                reportRecord.setBookId(book.getId());
                reportRecord.setPosition(currentPage);
                ReportCollectorTask reportCollectorTask = new ReportCollectorTask();
                reportCollectorTask.execute(reportRecord);
                break;
            }
            case R.id.print: {
                Intent printIntent = new Intent(PageActivity.this, PrintActivity.class);
                printIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                printIntent.putExtra(BOOK_ID, book.getId());
                printIntent.putExtra(BOOK_CONTEXT, context);
                startActivity(printIntent);
                break;
            }
            case R.id.preprocess: {
                context.setPreprocessing(!context.isPreprocessing());
                book.setPreprocessing(!book.getPreprocessing());
                context.setInvalidateCache(true);
                item.setIcon(book.getPreprocessing() ? R.drawable.ic_unenhance : R.drawable.ic_enhance);
                item.setTitle(book.getPreprocessing() ? R.string.unenhance : R.string.enhance );
            }
            case R.id.navigation: {
                if (pref.getBoolean(Constants.KINDLE_NAVIGATION, false)) {
                    pref.edit().putBoolean(Constants.KINDLE_NAVIGATION, false).apply();
                    item.setTitle(R.string.kindle_navigation);
                } else {
                    pref.edit().putBoolean(Constants.KINDLE_NAVIGATION, true).apply();
                    item.setTitle(R.string.ipad_navigation);
                }
            }
            case R.id.scrollbars: {
                if (pref.getBoolean(Constants.SHOW_SCROLLBARS, false)) {
                    pref.edit().putBoolean(Constants.SHOW_SCROLLBARS, false).apply();
                    scroll.setScrollBarSize(0);
                    item.setTitle(R.string.show_scrollbars);
                } else {
                    pref.edit().putBoolean(Constants.SHOW_SCROLLBARS, true).apply();
                    scroll.setScrollBarSize(50);
                    item.setTitle(R.string.hide_scrollbars);
                }
            }
        }
        setPageNumber(currentPage);
        return true;

    }

    public void setPageNumber(int pageNumber) {
        pager.setText(getString(R.string.ui_page_count, pageNumber + 1, book.getPagesCount()));
        seekBar.setProgress(pageNumber + 1);
        currentPage = pageNumber;
        book.setCurrentPage(pageNumber);
        booksCollection.updateBook(book);
        pageLoader = new PageLoader(this);
        kickOthers(pageLoader);
        Integer invCache = context.isInvalidateCache() ? 1 : 0;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int childCount = pageActivity.page.getChildCount();
        for (int k=0;k<childCount;k++) {
            View v = pageActivity.page.getChildAt(k);
            if (v instanceof ImageView) {
                ImageView iv = (ImageView)v;
                if (iv.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled() && book.getMode() != VIEW_MODE_ORIGINAL) {
//                        bitmap.recycle();
                    }
                }

            }
        }

        pageLoader.execute(pageNumber, invCache);

    }

    private void kickOthers(PageLoader pageLoader) {
        for (AsyncTask task : runningTasks) {
            if(!task.isCancelled()) {
                task.cancel(true);
                Log.d(getClass().getName(), "Cancelling taks #" + task.hashCode());
            }
        }
        runningTasks.add(pageLoader);
        Log.d(getClass().getName(), "Adding taks #" + pageLoader.hashCode());
    }

////////////////////////////   LISTENERS  ////////////////////////////////////////////////////

    class PagerTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.performClick();
            pager.setVisibility(GONE);
            seekBar.setVisibility(VISIBLE);
            return true;
        }
    }

    class PagerListener implements SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(getClass().getName(), String.format("onProgressChanged. %d %%", progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(getClass().getName(), "onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(getClass().getName(), "onStopTrackingTouch");
        int progress = seekBar.getProgress();
        setPageNumber(progress > 0 ? progress - 1: progress);
        seekBar.setVisibility(GONE);
        pager.setVisibility(VISIBLE);
    }
}

    class HomeButtonListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(PageActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }
    }

    class LayoutListener implements View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (pageRenderer == null) {
                try {
                    pageRenderer = PageRendererFactory.getRenderer(booksCollection, book);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            PageMenuListener pageMenuListener = new PageMenuListener();
            findViewById(R.id.smaller_text).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_text).setOnClickListener(pageMenuListener);
            scroll.scrollTo(0, book.getScrollOffset());
        }

    }

    class ShowListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            ImageView show = (ImageView)view;
            if (viewMode == VIEW_MODE_ORIGINAL) {
                viewMode = VIEW_MODE_PHONE;
                book.setMode(VIEW_MODE_PHONE);
                Drawable res = getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_menu_book_24);
                Log.d(getClass().getName(), "resource state is " + res.getConstantState().hashCode());
                show.setImageDrawable(res);
                Log.d(getClass().getName(),"button resource state is now " + show.getDrawable().getConstantState().hashCode());
                Snackbar.make(view, getString(R.string.ui_reflow_page), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.d(getClass().getName(), String.format("Setting page #%d for modified page", currentPage));
            } else if (viewMode == VIEW_MODE_PHONE) {
                viewMode = VIEW_MODE_ORIGINAL;
                book.setMode(VIEW_MODE_ORIGINAL);
                show.setImageResource(R.drawable.ic_baseline_smartphone_24);
                Snackbar.make(view, getString(R.string.ui_original_page), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.d(getClass().getName(), String.format("Setting page #%d for original page", currentPage));
            }
            pageActivity.setPageNumber(currentPage);
        }
    }

    class PageMenuListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (viewMode == VIEW_MODE_PHONE) {
                switch (v.getId()) {
                    case R.id.smaller_text: {
                        if (context.getZoom() <= Constants.ZOOM_MIN)
                            break;
                        context.setZoom(-1 * Constants.ZOOM_STEP + context.getZoom());
                        book.setZoom(context.getZoom());
                        break;
                    }
                    case R.id.larger_text: {
                        if (context.getZoom() > Constants.ZOOM_MAX)
                            break;
                        context.setZoom(Constants.ZOOM_STEP + context.getZoom());
                        book.setZoom(context.getZoom());
                        break;
                    }
                }
            } else {
                switch (v.getId()) {
                    case R.id.smaller_text: {
                        if (book.getZoomOriginal() <= Constants.ZOOM_MIN)
                            break;
                        context.setZoomOriginal(-1 * Constants.ZOOM_STEP + book.getZoomOriginal());
                        book.setZoomOriginal(context.getZoomOriginal());
                        break;
                    }
                    case R.id.larger_text: {
                        if (book.getZoomOriginal() > Constants.ZOOM_MAX)
                            break;
                        context.setZoomOriginal(Constants.ZOOM_STEP + book.getZoomOriginal());
                        book.setZoomOriginal(context.getZoomOriginal());
                        break;
                    }
                }
            }

            pageActivity.setPageNumber(currentPage);

        }

    }

//////////////////////////   ASYNC TASKS   /////////////////////////////////////////////////

    class ConnectionCheckerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            boolean result;
            try {
                URL url = new URL(REPORT_URL);
                connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                result = true;
            } catch (Exception e) {
                Log.e(getClass().getName(), "Failed to connect to server at " + REPORT_URL, e);
                result = false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return result;
        }

    }

    class PageLoader extends AsyncTask<Integer, Void, Void> {

        List<Bitmap> bitmaps;
        PageActivity pageActivity;

        private WeakReference<PageActivity> pageActivityReference;

        // only retain a weak reference to the activity
        PageLoader(PageActivity context) {
            pageActivityReference = new WeakReference<>(context);
            pageActivity = pageActivityReference.get();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pageActivity.runningTasks.remove(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pageActivity.runningTasks.remove(this);
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            runOnUiThread(()-> {
                    pageActivity.scroll.setVisibility(INVISIBLE);
                    pageActivity.progressBar.setVisibility(View.VISIBLE);
                }
            );
//
            int pageNumber = integers[0];
//
//            boolean invalidateCache = integers[1] == 1;
//
            if (pageActivity.viewMode == Constants.VIEW_MODE_PHONE) {
                bitmaps = new CopyOnWriteArrayList<>(pageActivity.pageRenderer.renderPage(context, pageNumber));
                Log.v(getClass().getName(), String.format("Get %d bitmaps for page %d", bitmaps.size(), pageNumber));
            } else {
                bitmaps = Arrays.asList(pageActivity.pageRenderer.renderOriginalPage(pageActivity.context, pageNumber));
            }
//
//            int bitmapHeight = bitmap.getHeight();
//
            runOnUiThread(() -> {
//
                List<View> pageViews = new ArrayList<>();// UI code goes here
                for (Bitmap bitmap : bitmaps) {
                    Log.d("FLOW-READER", "bitmaps " + bitmaps.size());
                    int bitmapHeight = bitmap.getHeight();
                    if (bitmap.getByteCount() > MAX_BITMAP_SIZE) {
                        Snackbar.make(topLayout, getString(R.string.could_not_zoom_more),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        context.setZoom(context.getZoom() - 0.5f);
                        pageActivity.book.setZoom(pageActivity.context.getZoom());
                        pageActivity.booksCollection.updateBook(pageActivity.book);
                        //} else if (bitmap.getWidth() >= pageActivity.context.getWidth()) {
                    } else {
                        ((ViewGroup)pageActivity.page.getParent()).removeView(pageActivity.page);
                        pageActivity.scroll.removeAllViews();
                        if (viewMode == VIEW_MODE_PHONE) {
                            for (int offset = 0; offset < bitmapHeight; offset += Constants.IMAGE_VIEW_HEIGHT_LIMIT) {
                                Log.d(getClass().getName(), "Before image creation");
                                int height = Math.min(bitmapHeight, offset + Constants.IMAGE_VIEW_HEIGHT_LIMIT);
                                Log.v(getClass().getName(),
                                        String.format(" Bitmap.createBitmap(bitmap, 0, %d, %d, %d)",
                                                offset, context.getWidth(), height - offset));
                                Log.v(getClass().getName(),
                                        String.format("bitmap size is width : %d height :%d",
                                                bitmap.getWidth(), bitmap.getHeight()));

                                Bitmap limitedBitmap = Bitmap.createBitmap(bitmap, 0, offset, context.getWidth(),
                                        height - offset);
                                if(darkTheme) {
                                    limitedBitmap = createInvertedBitmap(limitedBitmap);
                                }
                                ImageView imageView = new ImageView(getApplicationContext());
                                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                                imageView.setMaxHeight(Integer.MAX_VALUE);
                                imageView.setImageBitmap(limitedBitmap);
                                pageViews.add(imageView);
                                Log.d(getClass().getName(), "Image creation");
                                Log.d(getClass().getName(), "After image creation");
                            }
                            pageActivity.page.removeAllViews();
                            for (View view : pageViews) {
                                pageActivity.page.addView(view);
                            }
                            pageActivity.scroll.addView(pageActivity.page);
                            Log.v(getClass().getName(), "End setting bitmap");
                        } else {
                            inviteToTryReflow(bitmap);
                            pageActivity.page.removeAllViewsInLayout();
                            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getApplicationContext());
                            ImageView imageView = new ImageView(getApplicationContext());
                            Log.v(getClass().getName(), "Bitmap size is " + bitmap.getWidth() + "x" + bitmap.getHeight());
                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
                            imageView.setLayoutParams(layoutParams);
                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
                            //if (!bitmap.isRecycled()) {
                                imageView.setImageBitmap(bitmap);
                            //}

                            pageActivity.page.addView(imageView);
                            horizontalScrollView.addView(pageActivity.page);
                            pageActivity.scroll.addView(horizontalScrollView);
                            horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return PageActivity.this.onTouchEvent(event);
                                }
                            });
                        }
                    }
                }
                pageActivity.scroll.setVisibility(VISIBLE);
                pageActivity.progressBar.setVisibility(INVISIBLE);
                pageActivity.scroll.scrollTo(0, 0);

            });

            return null;

        }

        private void inviteToTryReflow(Bitmap bitmap) {
            if (bitmap.getWidth() <= context.getWidth()) {
                return;
            }
            if(pref.contains(Constants.SHOW_TRY_REFLOW) && !pref.getBoolean(Constants.SHOW_TRY_REFLOW, false)) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
            builder.setTitle(getResources().getString(R.string.try_reflow))
                    .setMessage(R.string.try_reflow_explained)
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_baseline_smartphone_24)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {dialog.cancel();})
                    .setNegativeButton(R.string.ok_not_anymore, (dialog, which) -> {
                        pref.edit().putBoolean(Constants.SHOW_TRY_REFLOW, false).apply();
                        dialog.cancel();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

//
    class ReportCollectorTask extends AsyncTask<ReportRecord, Void, Void> {

        @Override
        protected Void doInBackground(ReportRecord... reportRecords) {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            DaoAccess daoAccess = appDatabase.daoAccess();
            Long reportId = daoAccess.insertReport(reportRecords[0]);
            Intent reportIntent = new Intent(PageActivity.this, ReportActivity.class);
            reportIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            reportIntent.putExtra(REPORT_ID, reportId);
            reportIntent.putExtra(BOOK_ID, getBook().getId());
            reportIntent.putExtra(POSITION, getBook().getCurrentPage());
            startActivity(reportIntent);
            return null;
        }

    }

    private String getActionName(int code) {
        switch (code) {
            case 0: {return "ACTION_DOWN";}
            case 1: {return "ACTION_UP";}
            case 2: {return "ACTION_MOVE";}
            case 3: {return "ACTION_CANCEL";}
            case 4: {return "ACTION_OUTSIDE";}
            case 5: {return "ACTION_POINTER_DOWN";}
            case 6: {return "ACTION_POINTER_UP";}
            case 7: {return "ACTION_HOVER_MOVE";}
            case 8: {return "ACTION_SCROLL";}
            case 9: {return "ACTION_HOVER_ENTER";}
            case 10: {return "ACTION_HOVER_EXIT";}
            case 11: {return "ACTION_BUTTON_PRESS";}
            case 12: {return "ACTION_BUTTON_RELEASE";}
            case 261: {return "ACTION_POINTER_2_DOWN";}
            case 517: {return "ACTION_POINTER_3_DOWN";}
            case 262: {return "ACTION_POINTER_2_UP";}
            case 518: {return "ACTION_POINTER_3_UP";}
            default:{return "NO ACTION";}
        }
    }

}
