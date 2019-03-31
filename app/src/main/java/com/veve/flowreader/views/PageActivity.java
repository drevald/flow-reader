package com.veve.flowreader.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import org.opencv.android.OpenCVLoader;

import static android.view.View.VISIBLE;
import static com.veve.flowreader.Constants.VIEW_MODE_ORIGINAL;
import static com.veve.flowreader.Constants.VIEW_MODE_PHONE;

public class PageActivity extends AppCompatActivity {

    TextView pager;
    Toolbar toolbar;
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
    int currentPage;
    int viewMode;

    final static int IMAGE_VIEW_HEIGHT_LIMIT = 8000;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("", "Open CV init error");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.opencv_parser);
        menuItem.setChecked(true);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        int position = getIntent().getIntExtra("position", 0);
        book = BooksCollection.getInstance(getApplicationContext()).getBooks().get(position);
        pageRenderer = PageRendererFactory.getRenderer(book);
        currentPage = 0;

        viewMode = Constants.VIEW_MODE_PHONE;


        bar = findViewById(R.id.bar);
        topLayout = findViewById(R.id.topLayout);
        pager = findViewById(R.id.pager);
        seekBar = findViewById(R.id.slider);
        home = findViewById(R.id.home);
        progressBar = findViewById(R.id.progress);
        page = findViewById(R.id.page);

        page.addOnLayoutChangeListener(new LayoutListener());
        seekBar.setMax(book.getPagesCount());
        pager.setOnTouchListener(new PagerTouchListener());
        seekBar.setOnSeekBarChangeListener(new PagerListener());
        home.setOnClickListener(new HomeButtonListener());

        Display display = getWindowManager().getDefaultDisplay();
        context = new DevicePageContextImpl(display.getWidth());

        pageActivity = this;
        showPage(currentPage);

    }
//
    public void showPage(int pageNumber) {

        try {

            Log.v(getClass().getName(), "Start setting bitmap");
            BitmapGetter bitmapGetter = new BitmapGetter(pageRenderer);
            bitmapGetter.execute(pageNumber, viewMode, context);

            Bitmap bitmap = bitmapGetter.get();
            int bitmapHeight = bitmap.getHeight();

            page.removeAllViews();        // UI code goes here
            for (int offset = 0; offset < bitmapHeight; offset += IMAGE_VIEW_HEIGHT_LIMIT) {
                Log.d(getClass().getName(), String.format("Adding bitmap with offset %d", offset));
                int height = Math.min(bitmapHeight, offset + IMAGE_VIEW_HEIGHT_LIMIT);
                Bitmap limitedBitmap = Bitmap.createBitmap(bitmap, 0, offset, context.getWidth(),
                        height - offset);
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(limitedBitmap);
                page.addView(imageView);
            }

            Log.v(getClass().getName(), "End setting bitmap");

            page.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

        } catch (Exception e) {
            Log.d(getClass().getName(), "Failed to get bitmap");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.no_margins: {
                context.setMargin(0);
                break;
            }
            case R.id.narrow_margins: {
                context.setMargin(25);
                break;
            }
            case R.id.normal_margins: {
                context.setMargin(50);
                break;
            }
            case R.id.wide_margins: {
                context.setMargin(100);
                break;
            }
            case R.id.delete_book: {
                BooksCollection.getInstance(getApplicationContext()).deleteBook(book.getId());
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            }

        }

        showPage(currentPage);
        return true;

    }

    private void setPageNumber(int pageNumber, int totalPages) {
        pager.setText(getString(R.string.ui_page_count, pageNumber + 1, totalPages));
        seekBar.setProgress(pageNumber + 1);
        currentPage = pageNumber;
        book.setCurrentPage(currentPage);
        showPage(currentPage);
    }

////////////////////////////   LISTENERS  ////////////////////////////////////////////////////

    class PagerTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.performClick();
            pager.setVisibility(View.GONE);
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
            setPageNumber(seekBar.getProgress(), seekBar.getMax());
            seekBar.setVisibility(View.GONE);
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
//            context = new DevicePageContextImpl(v.getWidth());
            pageRenderer = PageRendererFactory.getRenderer(book);
            PageMenuListener pageMenuListener = new PageMenuListener();
            findViewById(R.id.smaller_text).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_text).setOnClickListener(pageMenuListener);
            findViewById(R.id.smaller_kerning).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_kerning).setOnClickListener(pageMenuListener);
            findViewById(R.id.smaller_leading).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_leading).setOnClickListener(pageMenuListener);
        }
    }

    class ShowListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            ImageView show = (ImageView)view;
            if (viewMode == VIEW_MODE_ORIGINAL) {
                viewMode = VIEW_MODE_PHONE;
                show.setImageResource(R.drawable.ic_phone);
                Snackbar.make(view, getString(R.string.ui_reflow_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (viewMode == VIEW_MODE_PHONE) {
                viewMode = VIEW_MODE_ORIGINAL;
                show.setImageResource(R.drawable.ic_book);
                Snackbar.make(view, getString(R.string.ui_original_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    class PageMenuListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.smaller_text: {
                    context.setZoom(0.8f * context.getZoom());
                    break;
                }
                case R.id.larger_text: {
                    context.setZoom(1.25f * context.getZoom());
                    break;
                }
                case R.id.smaller_kerning: {
                    context.setKerning(0.8f * context.getKerning());
                    break;
                }
                case R.id.larger_kerning: {
                    context.setKerning(1.25f * context.getKerning());
                    break;
                }
                case R.id.smaller_leading: {
                    context.setLeading(0.8f * context.getLeading());
                    break;
                }
                case R.id.larger_leading: {
                    context.setLeading(1.25f * context.getLeading());
                    break;
                }
            }

            pageActivity.showPage(currentPage);

        }


    }

    static class BitmapGetter extends AsyncTask<Object, Void, Bitmap> {

        PageRenderer renderer;

        BitmapGetter(PageRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        protected Bitmap doInBackground(Object... objects) {
            int position = (Integer)objects[0];
            int viewMode = (Integer)objects[1];
            DevicePageContext context = (DevicePageContext)objects[2];
            Bitmap bitmap;
            Log.d(getClass().getName(), String.format("Start rendering page %d", position));
            if (viewMode == Constants.VIEW_MODE_PHONE)
                bitmap = renderer.renderPage(context, position);
            else
                bitmap = renderer.renderOriginalPage(context, position);
            return bitmap;
        }
    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        TextViewHolder(View itemView) {
            super(itemView);
        }
    }

}
