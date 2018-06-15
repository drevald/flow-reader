package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static com.veve.flowreader.Constants.VIEW_MODE_ORIGINAL;
import static com.veve.flowreader.Constants.VIEW_MODE_PHONE;

public class PageActivity extends AppCompatActivity implements View.OnClickListener {

    TextView pager;

    Toolbar toolbar;

    AppBarLayout bar;

    CoordinatorLayout topLayout;

    int viewMode;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("", "Open CV init error");
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(getClass().getName(), "Button clicked");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMode = Constants.VIEW_MODE_PHONE;
        setContentView(R.layout.activity_page);
        toolbar = findViewById(R.id.toolbar);
        bar = findViewById(R.id.bar);
        topLayout = findViewById(R.id.topLayout);
        setSupportActionBar(toolbar);
        pager = findViewById(R.id.pager);
        FloatingActionButton home = findViewById(R.id.home);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("tag", "" + recyclerView.getWidth());
                if (recyclerView.getAdapter() == null) {

                    DevicePageContext pageContext = new DevicePageContextImpl(recyclerView.getWidth());
                    int position = getIntent().getIntExtra("position", 0);
                    BookRecord book = BooksCollection.getInstance(getApplicationContext()).getBooks().get(position);
                    PageListAdapter pageAdapter = new PageListAdapter(pageContext, book);
                    recyclerView.setAdapter(pageAdapter);

                    PageMenuListener pageMenuListener = new PageMenuListener();

                    ImageButton smallerTextButton = findViewById(R.id.smaller_text);
                    smallerTextButton.setOnClickListener(pageMenuListener);

                    ImageButton largerTextButton = findViewById(R.id.larger_text);
                    largerTextButton.setOnClickListener(pageMenuListener);

                    ImageButton smallerKerningButton = findViewById(R.id.smaller_kerning);
                    smallerKerningButton.setOnClickListener(pageMenuListener);

                    ImageButton largerKerningButton = findViewById(R.id.larger_kerning);
                    largerKerningButton.setOnClickListener(pageMenuListener);

                    ImageButton smallerLeadingButton = findViewById(R.id.smaller_leading);
                    smallerLeadingButton.setOnClickListener(pageMenuListener);

                    ImageButton largerLeadingButton = findViewById(R.id.larger_leading);
                    largerLeadingButton.setOnClickListener(pageMenuListener);

                }
            }
        });

        FloatingActionButton show = findViewById(R.id.show);

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                recyclerView.getRecycledViewPool().clear();
                recyclerView.setAdapter(null);
                recyclerView.invalidate();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final RecyclerView recyclerView = findViewById(R.id.list);
        PageActivity.PageListAdapter pageAdapter =
                (PageActivity.PageListAdapter) recyclerView.getAdapter();
        DevicePageContext context = pageAdapter.getContext();

        switch (item.getItemId()) {
            case R.id.no_margins: {
                context.setMargin(0);
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.narrow_margins: {
                context.setMargin(25);
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.normal_margins: {
                context.setMargin(50);
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.wide_margins: {
                context.setMargin(100);
                pageAdapter.notifyDataSetChanged();
                break;
            }
        }

        return true;

    }

    public void setPageNumber(int pageNumber, int totalPages) {
        pager.setText(getString(R.string.ui_page_count, pageNumber + 1, totalPages));
    }

    class PageMenuListener implements OnClickListener {

        final RecyclerView recyclerView = findViewById(R.id.list);
        PageActivity.PageListAdapter pageAdapter =
                (PageActivity.PageListAdapter) recyclerView.getAdapter();
        DevicePageContext context = pageAdapter.getContext();

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.smaller_text: {
                    context.setZoom(0.8f * context.getZoom());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
                case R.id.larger_text: {
                    context.setZoom(1.25f * context.getZoom());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
                case R.id.smaller_kerning: {
                    context.setKerning(0.8f * context.getKerning());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
                case R.id.larger_kerning: {
                    context.setKerning(1.25f * context.getKerning());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
                case R.id.smaller_leading: {
                    context.setLeading(0.8f * context.getLeading());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
                case R.id.larger_leading: {
                    context.setLeading(1.25f * context.getLeading());
                    pageAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }


    class PageListAdapter extends RecyclerView.Adapter {

        BookRecord book;

        PageRenderer renderer;

        DevicePageContext context;

        public PageListAdapter(DevicePageContext context, BookRecord book) {
            this.book = book;
            this.context = context;
            this.renderer = PageRendererFactory.getRenderer(book);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            //context.setDisplayDpi(metrics.densityDpi);
            context.setDisplayDpi(144);
        }

        public DevicePageContext getContext() {
            return context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(getClass().getName(), "onCreateViewHolder");
            ImageView view = new ImageView(PageActivity.this.getApplicationContext());
            return new TextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            PageActivity.this.setPageNumber(position, book.getPagesCount());
            Log.d(getClass().getName(), String.format("onBindViewHolder #%d", position));
            //BookPage bookPage = book.getPage(position);
            Bitmap bitmap;
            if (viewMode == Constants.VIEW_MODE_PHONE)
                bitmap = renderer.renderPage(context, position);
            else
                bitmap = renderer.renderOriginalPage(context, position);
            ((ImageView) holder.itemView).setImageBitmap(bitmap);
        }

        @Override
        public int getItemCount() {
            return book.getPagesCount();
        }

    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        public TextViewHolder(View itemView) {
            super(itemView);
        }
    }

}
