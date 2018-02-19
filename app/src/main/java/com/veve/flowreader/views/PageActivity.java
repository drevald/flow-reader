package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import android.view.ViewTreeObserver.*;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

public class PageActivity extends AppCompatActivity {

    TextView stubView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i("PageActivity", "Is toolbar visible" + getSupportActionBar().isShowing());

        stubView = new TextView(PageActivity.this.getApplicationContext());

        FloatingActionButton home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        final GridView gridView = findViewById(R.id.grid);
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("tag", ""+gridView.getWidth());
                if (gridView.getAdapter() == null) {
                    gridView.setAdapter(
                            new BookGridAdapter(
                                    new DevicePageContextImpl(gridView.getWidth())));
                }
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(getClass().getName(),
                        String.format("onScrollStateChanged scrollState=%d", scrollState));
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d(getClass().getName(), String.format("onScroll view=%S, firstVisibleItem=%d, visibleItemsCount=%d, totalItemsCount=%d",
                        view.toString(), firstVisibleItem, visibleItemCount, totalItemCount));
                for (int i=totalItemCount; i>firstVisibleItem+visibleItemCount;i--) {
                    recycleItem(view.getChildAt(i), i);
                }
                for (int i=0; i<firstVisibleItem;i++) {
                    recycleItem(view.getChildAt(i), i);
                }
            }
        });

    }

    private void recycleItem(View view, int position) {
        if (view == null || !(view instanceof ImageView))
            return;
        Drawable drawable = ((ImageView)view).getDrawable();
        if (drawable != null) {
             ((BitmapDrawable)drawable).getBitmap().recycle();
            Log.d(getClass().getName(), String.format("Bitmap successfully dumped at %d", position));
        }
        //view = stubView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final GridView gridView = findViewById(R.id.grid);
        BookGridAdapter pageAdapter = (BookGridAdapter)gridView.getAdapter();
        DevicePageContext context = pageAdapter.getPageContext();
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


    class BookGridAdapter extends BaseAdapter {

        public DevicePageContext getPageContext() {
            return pageContext;
        }

        public void setPageContext(DevicePageContext pageContext) {
            this.pageContext = pageContext;
        }

        DevicePageContext pageContext;

        Book book;

        Bitmap[] bitmaps;

        Bitmap bitmap;

        ImageView imageView, viewOne, viewTwo;

        View stubView;

        public BookGridAdapter(DevicePageContext context) {
            this.pageContext = context;
            this.stubView = new TextView(PageActivity.this.getApplicationContext());
            book = BooksCollection.getInstance().getBooks().get(0);
            bitmaps = new Bitmap[book.getPagesCount()];
        }

        @Override
        public int getCount() {
            return bitmaps.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            BookPage bookPage = book.getPage(position);
            bitmap = bookPage.getAsBitmap(pageContext);
            ImageView imageView = new ImageView(PageActivity.this.getApplicationContext());
            imageView.setImageBitmap(bitmap);
            Log.d(getClass().getName(), String.format("Getting view #%d", position));
            return imageView;

        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
//            Book book = BooksCollection.getInstance().getBooks().get(0);
//            bitmaps = new Bitmap[book.getPagesCount()];
//            for (int i=0; i<book.getPagesCount(); i++) {
//                BookPage bookPage = book.getPage(i);
//                bitmaps[i] = bookPage.getAsBitmap(pageContext);
//            }
        }
    }

}
