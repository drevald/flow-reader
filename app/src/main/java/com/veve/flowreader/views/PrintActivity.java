package com.veve.flowreader.views;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.veve.flowreader.Constants;
import com.veve.flowreader.PageTailor;
import com.veve.flowreader.PagesSet;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CancellationSignal;
import android.os.Debug;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import static android.graphics.Bitmap.Config.ARGB_4444;
import static com.veve.flowreader.Constants.INCH_IN_MILS;
import static com.veve.flowreader.Constants.MM_IN_INCH;

public class PrintActivity extends AppCompatActivity {

    static PageRenderer pageRenderer;

    static final PrintAttributes.MediaSize DEFAULT_MEDIA_SIZE = PrintAttributes.MediaSize.ISO_A4;

    static final int DEFAULT_RESOLUTION = 300;

    BookRecord bookRecord;

    List<PagesSet> pagesSets;

    EditText pages;

    DevicePageContext context;

    PrintJob printJob;

    RadioGroup columnGroup;

    RadioGroup pagesGroup;

    int screenWidthMm;

    class PrintBookAdapter extends PrintDocumentAdapter {

        BookRecord bookRecord;

        PrintAttributes attributes;

        PdfDocument pdfDocument;

        public PrintBookAdapter(Activity activity, BookRecord bookRecord) {
            Log.v(getClass().getName(), "Construct");
            this.bookRecord = bookRecord;
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.v(getClass().getName(), "on start");
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

            Log.v(getClass().getName(), "onWrite");


            // Write PDF document to file
            try {
                pdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                pdfDocument.close();
                pdfDocument = null;
            }


            //PageRange[] writtenPages = computeWrittenPages();
            // Signal the print framework the document is complete
            //callback.onWriteFinished(writtenPages);
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle extras) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Log.v(getClass().getName(), "onLayout");
                PrintDocumentInfo info = new PrintDocumentInfo
                        .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
//                    .setPageCount(10)
                        .build();
                attributes = newAttributes;
                pdfDocument = new PdfDocument();

                int colNum = 3;
                int widthInMils = attributes.getMediaSize().getWidthMils();
                int heightInMils = attributes.getMediaSize().getHeightMils();
                int workWidthInMils = widthInMils - attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils();
                int workHeightInMils = heightInMils - attributes.getMinMargins().getTopMils() - attributes.getMinMargins().getBottomMils();
                int gap = (attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils()) / colNum;
                workWidthInMils = workWidthInMils - gap * (colNum - 1);
                int columnsWidthInMils = workWidthInMils / colNum;
                int gapPixels = (int) (gap * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                int topMarginInPixel = (int) (attributes.getMinMargins().getTopMils() * INCH_IN_MILS * attributes.getResolution().getVerticalDpi());
                int leftMarginInPixel = (int) (attributes.getMinMargins().getLeftMils() * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                int columnsWithInPixels = (int) (columnsWidthInMils * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                int columnsHeightInPixels = (int) (workHeightInMils * INCH_IN_MILS * attributes.getResolution().getVerticalDpi());
                try {
                    context.setZoom(1);
                    Log.v(getClass().getName(), String.format("Setting column width %d hash %s",
                            columnsWithInPixels,
                            context.hashCode()));
                    context.setWidth(columnsWithInPixels);
                    Log.v(getClass().getName(),
                            String.format("Drawing column width %d on page %d(%d) x %d(%d) hash %s",
                                    context.getWidth(),
                                    (int)(attributes.getResolution().getHorizontalDpi() * INCH_IN_MILS * widthInMils),
                                    widthInMils,
                                    (int)(attributes.getResolution().getVerticalDpi() * INCH_IN_MILS * heightInMils),
                                    widthInMils,
                            context.hashCode()));

                    context.setScreenRatio(columnsWithInPixels / (float) columnsHeightInPixels);
                    PageTailor pageTailor = new PageTailor(pageRenderer, pagesSets, context, columnsHeightInPixels);
                    Bitmap bitmap;
                    int counter = 0;
                    PdfDocument.Page page = null;
                    while ((bitmap = pageTailor.read()) != null) {

                        // check for cancellation
                        if (cancellationSignal.isCanceled()) {
                            callback.onLayoutCancelled();
                            pdfDocument.close();
                            return;
                        }

                        int pageNum = (int) (counter / colNum);
                        PdfDocument.PageInfo.Builder pageBuilder = new PdfDocument.PageInfo.Builder(
                                (int) (attributes.getMediaSize().getWidthMils() * attributes.getResolution().getHorizontalDpi() * INCH_IN_MILS),
                                (int) (attributes.getMediaSize().getHeightMils() * attributes.getResolution().getVerticalDpi() * INCH_IN_MILS),
                                pageNum);
                        if (counter % colNum == 0) {
                            page = pdfDocument.startPage(pageBuilder.create());
                        }
                        Log.v(getClass().getName(), String.format("Drawing column from x:%d y:%d w:%d h:%d on page %d",
                                leftMarginInPixel + ((counter % colNum) * (leftMarginInPixel + columnsWithInPixels)),
                                topMarginInPixel,
                                bitmap.getWidth(),
                                bitmap.getHeight(),
                                pageNum));
                        page.getCanvas().drawBitmap(bitmap, leftMarginInPixel + ((counter % colNum) * (leftMarginInPixel + columnsWithInPixels)), topMarginInPixel, null);
                        if (Constants.DEBUG) {
                            page.getCanvas().drawRect(leftMarginInPixel + ((counter % colNum) * (leftMarginInPixel + columnsWithInPixels)), topMarginInPixel, bitmap.getWidth(), bitmap.getHeight(), new Paint());
                        }

                        if ((counter + 1) % colNum == 0) {
                            Log.v(getClass().getName(), String.format("Closing page %d, counter is %d number of columns %d", pageNum, counter, colNum));
                            pdfDocument.finishPage(page);
                        }

                        counter++;

                    }

                    try {
                        pdfDocument.finishPage(page);
                    } catch (Exception e) {
                        Log.e(getClass().getName(), e.getLocalizedMessage());
                    }
                    callback.onLayoutFinished(info, false);
                    Log.v(getClass().getName(), "onLayout 1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doPrint(BookRecord bookRecord) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getActivity().getString(R.string.app_name) + "_" + bookRecord.getTitle();

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        PrintAttributes printAttributes = builder.build();
        printJob = printManager.print(jobName, new PrintBookAdapter(getActivity(), bookRecord), printAttributes); //

    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookRecordId = getIntent().getLongExtra(Constants.BOOK_ID, -1);
        context = (DevicePageContext) getIntent().getSerializableExtra(Constants.BOOK_CONTEXT);
        bookRecord = BooksCollection.getInstance(getApplicationContext()).getBook(bookRecordId);

        try {
            pageRenderer = PageRendererFactory.getRenderer(BooksCollection.getInstance(getApplicationContext()), bookRecord);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
        }

        findViewById(R.id.print).setOnClickListener((view) -> {
                doPrint(bookRecord);
            }
        );

        findViewById(R.id.cancel).setOnClickListener((view) -> {
            if (printJob != null && printJob.isStarted() || printJob.isQueued())
                printJob.cancel();
            }
        );

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        pagesGroup = findViewById(R.id.page_range);
        pagesGroup.check(R.id.custom_page_range);
        pages = ((EditText) findViewById(R.id.pages));
        pages.setText("1-10");
        parsePagesString();
        pages.setOnFocusChangeListener((v, hasFocus) -> {
                parsePagesString();
        });

        columnGroup = findViewById(R.id.column);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float widthInInches = displayMetrics.widthPixels / (float) displayMetrics.xdpi;
        float heightInInches = displayMetrics.heightPixels / (float) displayMetrics.ydpi;
        screenWidthMm =  (int) ((Math.min(widthInInches, heightInInches) * MM_IN_INCH));
        setPrintDeviceScreen(findViewById(R.id.set_column_width_as_device));
        findViewById(R.id.column_width).setOnFocusChangeListener((v, hasFocus) -> {
            Log.v(getClass().getName(), "Column width input set to " + ((EditText)v).getText().toString());
            columnGroup.check(R.id.set_column_width);
            try {
                int colsNum = calculateColumnsNum(Integer.parseInt(((EditText)findViewById(R.id.column_width)).getText().toString().trim()), DEFAULT_MEDIA_SIZE);
                ((EditText)findViewById(R.id.columns_number)).setText(String.valueOf(colsNum));
                findViewById(R.id.column_width).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } catch (Exception e) {
                findViewById(R.id.column_width).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        });
        findViewById(R.id.columns_number).setOnFocusChangeListener((v, hasFocus) -> {
            Log.v(getClass().getName(), "Column number set to " + ((EditText)v).getText().toString());
            columnGroup.check(R.id.set_columns_number);
            try {
                int colsNum = Integer.parseInt(((EditText)v).getText().toString());
                if (colsNum < 1) throw new Exception("Number of columns could not be less than one");
                int columnWidth = calculateColumnWidthMm(Integer.parseInt(((EditText)v).getText().toString().trim()), DEFAULT_MEDIA_SIZE);
                ((EditText)findViewById(R.id.column_width)).setText(String.valueOf(columnWidth));
                findViewById(R.id.columns_number).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } catch (Exception e) {
                findViewById(R.id.columns_number).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        });
    }

    private void parsePagesString() {
        try {
            pagesSets = PagesSet.getPagesSet((pages).getEditableText().toString());
            pages.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } catch (NumberFormatException e) {
            pages.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public void setPrintCurrentPage(View view) {
        pagesGroup.check(R.id.current_page);
        ((EditText)findViewById(R.id.pages)).setText(String.valueOf(1 + bookRecord.getCurrentPage()));
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintAllPages(View view) {
        pagesGroup.check(R.id.all_pages);
        ((EditText)findViewById(R.id.pages)).setText("1-" + bookRecord.getPagesCount());
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintCustomRange(View view) {
        pagesGroup.check(R.id.custom_page_range);
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_CLASS_TEXT);
        parsePagesString();
    }

    //////////////////

    public void setPrintDeviceScreen(View view) {
        columnGroup.check(R.id.set_column_width_as_device);
        ((EditText)findViewById(R.id.column_width)).setText(String.valueOf(screenWidthMm));
        ((EditText)findViewById(R.id.columns_number)).setText(String.valueOf(calculateColumnsNum(screenWidthMm, DEFAULT_MEDIA_SIZE)));
    }

    public void setColumnWidth(View view) {
        columnGroup.check(R.id.set_column_width);
    }


    public void setColumnsNumber(View view) {
        columnGroup.check(R.id.set_columns_number);
    }

    private int calculateColumnsNum(int columnWidthMm, PrintAttributes.MediaSize mediaSize) {
        int mediaWidthMm = (int)(mediaSize.getWidthMils() * MM_IN_INCH * INCH_IN_MILS);
        int result = (int)Math.floor(mediaWidthMm / columnWidthMm);
        Log.v(getClass().getName(), String.format("Num of columns for w:%d mm Media.w:%f is %d",
                columnWidthMm, mediaSize.getWidthMils() * MM_IN_INCH * INCH_IN_MILS, result));
        return result;
    }

    private int calculateColumnWidthMm(int columnsNumber, PrintAttributes.MediaSize mediaSize) {
        int result = (int)(Math.floor((mediaSize.getWidthMils() * MM_IN_INCH * INCH_IN_MILS )/columnsNumber));
        Log.v(getClass().getName(), String.format("Width of columns, mm for n:%d Media.w:%f is %d",
                columnsNumber, mediaSize.getWidthMils() * MM_IN_INCH * INCH_IN_MILS, result));
        return result;
    }

}
