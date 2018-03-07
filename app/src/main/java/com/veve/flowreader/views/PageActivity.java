package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

public class PageActivity extends AppCompatActivity {

    TextView pager;

    Toolbar toolbar;

    AppBarLayout bar;

    CoordinatorLayout topLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        FloatingActionButton show = findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bar.getVisibility() == View.VISIBLE) {
                    bar.setVisibility(View.GONE);
                    setSupportActionBar(null);
                } else {
                    bar.setVisibility(View.VISIBLE);
                }
                topLayout.requestLayout();
                topLayout.forceLayout();
                topLayout.invalidate();
                //topLayout.onInvalidate();
                //setContentView(R.layout.activity_page);

            }
        });

        final RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("tag", ""+recyclerView.getWidth());
                if (recyclerView.getAdapter() == null) {
                    recyclerView.setAdapter(
                            new PageActivity.TestListAdapter(
                                    new DevicePageContextImpl(recyclerView.getWidth())));
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.i(getClass().getName(), String.format("%S %d", recyclerView.toString(), newState));
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.i(getClass().getName(), String.format("%S %d %d", recyclerView.toString(), dx, dy));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final RecyclerView recyclerView = findViewById(R.id.list);
        PageActivity.TestListAdapter pageAdapter = (PageActivity.TestListAdapter)recyclerView.getAdapter();
        DevicePageContext context = pageAdapter.getContext();
        switch (item.getItemId()) {
            case R.id.decrease_font: {
                context.setZoom(0.8f*context.getZoom());
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.increase_font: {
                context.setZoom(1.25f*context.getZoom());
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.decrease_kerning: {
                context.setKerning(0.8f*context.getKerning());
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.increase_kerning: {
                context.setKerning(1.25f*context.getKerning());
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.decrease_leading: {
                context.setLeading(0.8f*context.getLeading());
                pageAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.increase_leading: {
                context.setLeading(1.25f*context.getLeading());
                pageAdapter.notifyDataSetChanged();
                break;
            }
        }
        return true;
    }

    public void setPageNumber(int pageNumber, int totalPages) {
        pager.setText(String.format("Page #%d of %d", pageNumber + 1, totalPages));
    }


    class TestListAdapter extends RecyclerView.Adapter {

        Book book;

        DevicePageContext context;

        public TestListAdapter(DevicePageContext context) {
            this.book = BooksCollection.getInstance().getBooks().get(0);
            this.context = context;
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
            BookPage bookPage = book.getPage(position);
            Bitmap bitmap = bookPage.getAsBitmap(context);
            ((ImageView)holder.itemView).setImageBitmap(bitmap);
        }

        @Override
        public int getItemCount() {
            return 600;
        }

    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        public TextViewHolder(View itemView) {
            super(itemView);
        }
    }

}
