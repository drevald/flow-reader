package com.veve.flowreader.views;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.veve.flowreader.Constants.BOOK_ID;
import static com.veve.flowreader.Constants.MAX_BITMAP_SIZE;
import static com.veve.flowreader.Constants.VIEW_MODE_ORIGINAL;
import static com.veve.flowreader.Constants.VIEW_MODE_PHONE;

public class PageActivity extends AppCompatActivity {

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
    int currentPage;
    int viewMode;
    BooksCollection booksCollection;
    LinearLayout bottomBar;
    boolean barsVisible;
    String commitId = "$Id$";
    PageLoader pageLoader;

    @Override
    protected void onPause() {
        super.onPause();
        book.setScrollOffset(scroll.getScrollY());
        booksCollection.updateBook(book);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.page_menu, menu);
        if(menu instanceof MenuBuilder){
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
        Log.d("INTENT_ONNEWINTENT",  getIntent().getIntExtra(BOOK_ID, 0)  + " = getIntent().getLongExtra(Constants.BOOK_ID, 0); hash = " + intent.hashCode());;
        int position = getIntent().getIntExtra("position", 0);
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        booksCollection.updateBook(book);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.v(getClass().getName(), getClass().getName() + "onCreate# " + this.hashCode());

        runningTasks = new HashSet<>();

        setContentView(R.layout.activity_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookId = getIntent().getLongExtra(BOOK_ID, 0);
        Log.d("INTENT_ONCREATE", bookId + " = getIntent().getLongExtra(Constants.BOOK_ID, 0); hash = " + getIntent().hashCode());
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(bookId);

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

        seekBar.setMax(book.getPagesCount());
        pager.setOnTouchListener(new PagerTouchListener());
        seekBar.setOnSeekBarChangeListener(new PagerListener());
        home.setOnClickListener(new HomeButtonListener());
        show.setOnClickListener(new ShowListener());
        page.setOnTouchListener(new OnDoubleTapListener(this, page));
        topLayout.addOnLayoutChangeListener(new LayoutListener());

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        context = new DevicePageContextImpl(point.x);
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


        TextView bookTitle = findViewById(R.id.book_title);
        bookTitle.setText(book.getTitle());
        bookTitle.setOnClickListener((view)->{
            AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
            builder.setCancelable(false)
                    .setMessage(book.getTitle())
                    .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });



        pageActivity = this;
        setPageNumber(currentPage);

        book.setZoom(context.getZoom());

        //runOnUiThread(()-> {
            show.setImageResource(viewMode ==
                    Constants.VIEW_MODE_PHONE ? R.drawable.ic_to_book : R.drawable.ic_to_phone);
        //});

        //book.getPreprocessing() ?

    }

    public BookRecord getBook() {
        return book;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.preprocess);
        item.setIcon(book.getPreprocessing() ? R.drawable.ic_unenhance : R.drawable.ic_enhance);
        item.setTitle(book.getPreprocessing() ? R.string.unenhance : R.string.enhance );
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        context.setInvalidateCache(false);
        switch (item.getItemId()) {
//            case R.id.margins_minus: {
//                int margin = context.getMargin();
//                context.setMargin(margin > MARGIN_STEP ? margin - MARGIN_STEP : margin);
//                book.setMargin(margin);
//                break;
//            }
//            case R.id.margins_plus: {
//                int margin = context.getMargin();
//                context.setMargin(margin < MARGIN_MAX ? margin + MARGIN_STEP : margin);
//                book.setMargin(margin);
//                break;
//            }
//
//            case R.id.kerning_minus: {
//                context.setKerning(0.8f * context.getKerning());
//                Log.v(getClass().getTitle(), "Kerning set to " + context.getKerning());
//                book.setKerning(context.getKerning());
//                break;
//            }
//            case R.id.kerning_plus: {
//                context.setKerning(1.25f * context.getKerning());
//                Log.v(getClass().getTitle(), "Kerning set to " + context.getKerning());
//                book.setKerning(context.getKerning());
//                break;
//            }
//            case R.id.leading_minus: {
//                context.setLeading(0.8f * context.getLeading());
//                Log.v(getClass().getTitle(), "Leading set to " + context.getLeading());
//                book.setLeading(context.getLeading());
//                break;
//            }
//            case R.id.leading_plus: {
//                context.setLeading(1.25f * context.getLeading());
//                Log.v(getClass().getTitle(), "Leading set to " + context.getLeading());
//                book.setLeading(context.getLeading());
//                break;
//            }
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
                Bitmap originalBitmap = pageRenderer.renderOriginalPage(currentPage);
                Bitmap reflowedBitmap = pageLoader.bitmap;
                ByteArrayOutputStream osOriginal = new ByteArrayOutputStream();
                ByteArrayOutputStream osReflowed = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, osOriginal);
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
                startActivity(printIntent);
                break;
            }
            case R.id.delete_book: {

                AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
                builder.setTitle(R.string.book_deletion)
                        .setMessage(String.format(
                                getResources().getString(R.string.confirm_delete), book.getTitle()))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long bookId = book.getId();
                                BooksCollection.getInstance(getApplicationContext()).deleteBook(bookId);
                                Intent i = new Intent(PageActivity.this, MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                i.putExtra(BOOK_ID, bookId);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            }
            case R.id.preprocess: {
                context.setPreprocessing(!context.isPreprocessing());
                book.setPreprocessing(!book.getPreprocessing());
                context.setInvalidateCache(true);
                item.setIcon(book.getPreprocessing() ? R.drawable.ic_unenhance : R.drawable.ic_enhance);
                item.setTitle(book.getPreprocessing() ? R.string.unenhance : R.string.enhance );
            }
        }
        setPageNumber(currentPage);
        return true;

    }

    private void setPageNumber(int pageNumber) {
        pager.setText(getString(R.string.ui_page_count, pageNumber + 1, book.getPagesCount()));
        seekBar.setProgress(pageNumber + 1);
        currentPage = pageNumber;
        book.setCurrentPage(pageNumber);
        booksCollection.updateBook(book);
        pageLoader = new PageLoader(this);
        kickOthers(pageLoader);
        Integer invCache = context.isInvalidateCache() ? 1 : 0;
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

    class SwapListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            float x1, x2;
            float MIN_DISTANCE = 150;
            x1 = 0;
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if (x2 > x1) {
                            Log.d(getClass().getName(),"Left to Right swipe [Next]");
                        } else {
                            Log.d(getClass().getName(),"Right to Left swipe [Next]");
                        }
                    }
                    break;
            }
            view.onTouchEvent(event);
            return true;
//                return true;
        }
    }

    class PagerTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.performClick();
            pager.setVisibility(GONE);
            seekBar.setVisibility(VISIBLE);
            return true;
        }
    }

    class OnDoubleTapListener implements View.OnTouchListener {

        private GestureDetector gestureDetector;

        OnDoubleTapListener(Context c, LinearLayout p) {
            gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
                    Log.v(getClass().getName(), "onScroll");
                    if (Math.abs(distanceX) > 2 * Math.abs(distanceY) && Math.abs(distanceX) > 50) {
                        if (distanceX > 0) {
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
                    try {
                        Thread.currentThread().sleep(100);
                    } catch (Exception e) {

                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    float x = e.getX();

                    if (x > p.getWidth() / 2) {
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

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

            });
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.performClick();
            return gestureDetector.onTouchEvent(motionEvent);
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
                Drawable res = getApplicationContext().getResources().getDrawable(R.drawable.ic_to_book);
                Log.d(getClass().getName(), "resource state is " + res.getConstantState().hashCode());
                //show.setImageResource(R.drawable.ic_to_book);
                show.setImageDrawable(res);
                Log.d(getClass().getName(),
                        "button resource state is now " + show.getDrawable().getConstantState().hashCode());
                Snackbar.make(view, getString(R.string.ui_reflow_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d(getClass().getName(), String.format("Setting page #%d for modified page", currentPage));
            } else if (viewMode == VIEW_MODE_PHONE) {
                viewMode = VIEW_MODE_ORIGINAL;
                book.setMode(VIEW_MODE_ORIGINAL);
                show.setImageResource(R.drawable.ic_to_phone);
                Snackbar.make(view, getString(R.string.ui_original_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

    class PageLoader extends AsyncTask<Integer, Void, Void> {

        Bitmap bitmap;
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

            int pageNumber = integers[0];

            boolean invalidateCache = integers[1] == 1;

            if (pageActivity.viewMode == Constants.VIEW_MODE_PHONE) {
                bitmap = pageActivity.pageRenderer.renderPage(context, pageNumber);
            } else {
                bitmap = pageActivity.pageRenderer.renderOriginalPage(pageActivity.context, pageNumber);
            }

            int bitmapHeight = bitmap.getHeight();

            runOnUiThread(() -> {
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
                        List<View> pageViews = new ArrayList<>();// UI code goes here
                        for (int offset = 0; offset < bitmapHeight; offset += Constants.IMAGE_VIEW_HEIGHT_LIMIT) {
                            Log.d(getClass().getName(), "Before image creation");
                            int height = Math.min(bitmapHeight, offset + Constants.IMAGE_VIEW_HEIGHT_LIMIT);
                            Log.v(getClass().getName(),
                                    String.format(" Bitmap.createBitmap(bitmap, 0, %d, %d, %d)",
                                            offset, context.getWidth(), height - offset));
                            Log.v(getClass().getName(),
                                    String.format("bitmap size is width : %d height :%d",
                                            bitmap.getWidth(), bitmap.getHeight()));

                            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                            activityManager.getMemoryInfo(mi);
                            Log.v("BITMAP_MEMORY", "mi.availMem " + mi.availMem);
                            Log.v("BITMAP_MEMORY", "mi.totalMem " + mi.totalMem);
                            Log.v("BITMAP_MEMORY", "mi.lowMemory " + mi.lowMemory);
//                            Log.v("BITMAP_MEMORY", "mi.visibleAppThreshold " + mi.hiddenAppThreshold);

                            Bitmap limitedBitmap = Bitmap.createBitmap(bitmap, 0, offset, context.getWidth(),
                                    height - offset);
                            ImageView imageView = new ImageView(getApplicationContext());
                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
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
                        pageActivity.page.removeAllViewsInLayout();
                        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getApplicationContext());
                        ImageView imageView = new ImageView(getApplicationContext());
                        Log.v(getClass().getName(), "Bitmap size is " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
                        imageView.setLayoutParams(layoutParams);
                        imageView.setScaleType(ImageView.ScaleType.FIT_START);
                        imageView.setImageBitmap(bitmap);
                        pageActivity.page.addView(imageView);
                        horizontalScrollView.addView(pageActivity.page);
                        pageActivity.scroll.addView(horizontalScrollView);
                    }
                }
                pageActivity.scroll.setVisibility(VISIBLE);
                pageActivity.progressBar.setVisibility(INVISIBLE);
                pageActivity.scroll.scrollTo(0, 0);
            });
            return null;
        }
    }

    class ReportCollectorTask extends AsyncTask<ReportRecord, Void, Void> {

        @Override
        protected Void doInBackground(ReportRecord... reportRecords) {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            DaoAccess daoAccess = appDatabase.daoAccess();
            Long reportId = daoAccess.insertReport(reportRecords[0]);
            Intent reportIntent = new Intent(PageActivity.this, ReportActivity.class);
            reportIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            reportIntent.putExtra("reportId", reportId);
            startActivity(reportIntent);
            return null;
        }

    }

}
