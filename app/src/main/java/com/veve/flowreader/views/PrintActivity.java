package com.veve.flowreader.views;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import com.veve.flowreader.Constants;
import com.veve.flowreader.PageTailor;
import com.veve.flowreader.PagesSet;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.veve.flowreader.Constants.DEFAULT_MEDIA_SIZE;
import static com.veve.flowreader.Constants.INCH_IN_MILS;
import static com.veve.flowreader.Constants.INCH_IN_MM;
import static com.veve.flowreader.Constants.MILS_IN_MM;
import static com.veve.flowreader.Constants.MM_IN_INCH;
import static com.veve.flowreader.Constants.MM_IN_MILS;

public class PrintActivity extends AppCompatActivity {

    static PageRenderer pageRenderer;

    static PrintAttributes DEFAULT_ATTRIBUTES;

    static {
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(Constants.DEFAULT_MEDIA_SIZE);
        builder.setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0));
        builder.setResolution(new PrintAttributes.Resolution("300dpi", "300dpi", 300, 300));
        DEFAULT_ATTRIBUTES = builder.build();
    }

    BookRecord bookRecord;
    List<PagesSet> pagesSets;
    EditText pages;
    DevicePageContext context;
    PrintJob printJob;
    RadioGroup columnGroup;
    RadioGroup pagesGroup;
    int screenWidthMm;
    int colNum;
    int columnsWidthInPixels;
    int gapWidthPx;
    PrintAttributes printAttributes;
    float zoom = 1f;

    class PrintBookAdapter extends PrintDocumentAdapter {

        BookRecord bookRecord;

        PrintAttributes attributes;

        PdfDocument pdfDocument;

        PrintBookAdapter(BookRecord bookRecord) {
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
            Log.v("PRINT", "4");
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
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle extras) {

            Log.v("PRINT", "3");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                Log.v(getClass().getName(), "onLayout");
                PrintDocumentInfo info = new PrintDocumentInfo
                        .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build();
                attributes = newAttributes;
                pdfDocument = new PdfDocument();

                setColumnLayout(attributes);

                int widthInMils = attributes.getMediaSize().getWidthMils();
                int heightInMils = attributes.getMediaSize().getHeightMils();
                int workWidthInMils = widthInMils - attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils();
                int workHeightInMils = heightInMils - attributes.getMinMargins().getTopMils() - attributes.getMinMargins().getBottomMils();
                int gap = (attributes.getMinMargins().getLeftMils()
                        - attributes.getMinMargins().getRightMils()) / colNum;
                workWidthInMils = workWidthInMils - gap * (colNum - 1);
                int columnsWidthInMils = workWidthInMils / colNum;
                gapWidthPx = (int) (gap * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                int topMarginInPixel = (int) (attributes.getMinMargins().getTopMils() * INCH_IN_MILS * attributes.getResolution().getVerticalDpi());
                int leftMarginInPixel = (int) (attributes.getMinMargins().getLeftMils() * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                columnsWidthInPixels = (int) (columnsWidthInMils * INCH_IN_MILS * attributes.getResolution().getHorizontalDpi());
                int columnsHeightInPixels = (int) (workHeightInMils * INCH_IN_MILS * attributes.getResolution().getVerticalDpi());

                try {
                    context.setZoom(zoom);
                    Log.v(getClass().getName(), String.format("Setting column width %d hash %s",
                            columnsWidthInPixels,
                            context.hashCode()));
                    context.setWidth(columnsWidthInPixels);
                    Log.v(getClass().getName(),
                            String.format("Drawing column width %d on page %d(%d) x %d(%d) hash %s",
                                    context.getWidth(),
                                    (int)(attributes.getResolution().getHorizontalDpi() * INCH_IN_MILS * widthInMils),
                                    widthInMils,
                                    (int)(attributes.getResolution().getVerticalDpi() * INCH_IN_MILS * heightInMils),
                                    heightInMils,
                            context.hashCode()));

                    context.setScreenRatio(columnsWidthInPixels / (float) columnsHeightInPixels);
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
                            Log.v(getClass().getName(), "Drawing " + counter + " % " + colNum + " == " + (counter % colNum));
                            page = pdfDocument.startPage(pageBuilder.create());
                        }
                        Log.v(getClass().getName(), String.format("Drawing column from x:%d y:%d w:%d h:%d on page %d",
                                leftMarginInPixel + ((counter % colNum) * (leftMarginInPixel + columnsWidthInPixels)),
                                topMarginInPixel,
                                bitmap.getWidth(),
                                bitmap.getHeight(),
                                pageNum));
                        page.getCanvas().drawBitmap(bitmap, leftMarginInPixel + ((counter % colNum) * (gapWidthPx + columnsWidthInPixels)), topMarginInPixel, null);
                        if (Constants.DEBUG) {
                            page.getCanvas().drawRect(leftMarginInPixel + ((counter % colNum) * (gapWidthPx + columnsWidthInPixels)), topMarginInPixel, bitmap.getWidth(), bitmap.getHeight(), new Paint());
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
                    callback.onLayoutFinished(info, true);
                    Log.v(getClass().getName(), "onLayout 1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setColumnLayout(PrintAttributes attributes) {
        int selectedId = columnGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.set_column_width_as_device || selectedId == R.id.set_column_width)  {
            colNum = calculateColsNum(getInt(R.id.column_width), attributes);
            gapWidthPx = calculateGapPix(getInt(R.id.column_width), attributes);
            columnsWidthInPixels = (int)(getInt(R.id.column_width) * INCH_IN_MM
                    * attributes.getResolution().getHorizontalDpi());
        } else if (selectedId == R.id.set_columns_number) {
            columnsWidthInPixels = calculateColumnWidthPix(
                    getInt(R.id.columns_number), getInt(R.id.gap), attributes);
            colNum = getInt(R.id.columns_number);
            gapWidthPx = (int)(getInt(R.id.gap) * INCH_IN_MM
                    * attributes.getResolution().getHorizontalDpi());
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
        builder.setMediaSize(DEFAULT_MEDIA_SIZE);
        PrintAttributes printAttributes = builder.build();
        Log.v("PRINT", "1");
        printJob = printManager.print(jobName, new PrintBookAdapter(bookRecord), printAttributes); //
        Log.v("PRINT", "2");

    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printAttributes = DEFAULT_ATTRIBUTES;
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

        findViewById(R.id.do_print).setOnClickListener((view) -> {
                parsePagesString();
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
        pagesGroup.check(R.id.current_page);
        pages = ((EditText) findViewById(R.id.pages));
        pages.setText(String.valueOf(1 + bookRecord.getCurrentPage()));
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

        ((RadioGroup)findViewById(R.id.font_size)).check(R.id.font_size_full);

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
        ((EditText) findViewById(R.id.column_width)).setText(String.valueOf(screenWidthMm));
        ((EditText) findViewById(R.id.columns_number)).setText(
                String.valueOf(calculateColsNum(screenWidthMm, printAttributes))
        );
        ((EditText) findViewById(R.id.gap)).setText(
                String.valueOf(calculateGapMm(screenWidthMm, printAttributes))
        );
        ((EditText) findViewById(R.id.columns_number)).setInputType(EditorInfo.TYPE_NULL);
        ((EditText) findViewById(R.id.gap)).setInputType(EditorInfo.TYPE_NULL);
    }

    public void setColumnWidth(View view) {
        columnGroup.check(R.id.set_column_width);
        if (getInt(R.id.gap) < 0 || getInt(R.id.columns_number) < 0 ) return;
        ((EditText) findViewById(R.id.column_width)).setText(
                String.valueOf(calculateColumnWidthMm(
                        getInt(R.id.columns_number),
                        getInt(R.id.gap),
                        printAttributes))
        );
//        ((EditText) findViewById(R.id.columns_number)).setText("");
        ((EditText) findViewById(R.id.gap)).setText("");
        ((EditText) findViewById(R.id.columns_number)).setInputType(EditorInfo.TYPE_NULL);
        ((EditText) findViewById(R.id.gap)).setInputType(EditorInfo.TYPE_NULL);
        ((EditText) findViewById(R.id.column_width)).setInputType(EditorInfo.TYPE_CLASS_NUMBER);
    }

    public void setColumnsNumber(View view) {
        columnGroup.check(R.id.set_columns_number);
        if (getInt(R.id.column_width) < 0) return;
        ((EditText) findViewById(R.id.columns_number)).setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        ((EditText) findViewById(R.id.gap)).setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        ((EditText) findViewById(R.id.columns_number)).setText(
                String.valueOf(calculateColsNum(getInt(R.id.column_width), printAttributes))
        );
        ((EditText) findViewById(R.id.gap)).setText(
                String.valueOf(calculateGapMm(getInt(R.id.column_width), printAttributes))
        );
//        ((EditText) findViewById(R.id.column_width)).setText("");
        ((EditText) findViewById(R.id.column_width)).setInputType(EditorInfo.TYPE_NULL);
    }

    private int getInt(int id) {
        int result;
        EditText v = (EditText)findViewById(id);
        try {
            result = Integer.parseInt(v.getText().toString());
            v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } catch (Exception e) {
            v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            result = -1;
        }
        return result;
    }

    /**
     * Calculates printable column width for given media, cols number and gap
     * @param columnsNumber number of columns to print
     * @param gap - gap between columns in millimeters
     * @param attributes - print attributes including media size and resolution
     * @return - column width in millimeters
     */
    public int calculateColumnWidthPix(int columnsNumber, int gap, PrintAttributes attributes) {
        int result = calculateColumnWidthMm(columnsNumber, gap, attributes);
        result *= INCH_IN_MM * attributes.getResolution().getHorizontalDpi();
        return result;
    }

    private int calculateColumnWidthMm(int columnsNumber, int gap, PrintAttributes attributes) {
        int workingWidthMils =
                attributes.getMediaSize().getWidthMils()
                        - attributes.getMinMargins().getLeftMils()
                        - attributes.getMinMargins().getRightMils()
                        - (int) (gap * (columnsNumber - 1) * MILS_IN_MM);
        int result = (int) (workingWidthMils  * MM_IN_MILS / (float)columnsNumber);
//        Log.v(getClass().getName(), String.format("Width of columns, mm for n:%d and gap %dmm for page.w:%f is %d",
//                columnsNumber, gap, attributes.getMediaSize().getWidthMils() * MM_IN_MILS, MM_IN_INCH * result));
        return result;
    }

    /**
     * Calculates gap in mm for given media and column width
     * @param columnWidthMm - column width in mm
     * @param attributes - print attributes
     * @return - gap width in mm
     */
    public int calculateGapPix(int columnWidthMm, PrintAttributes attributes) {
        int result = calculateGapMm(columnWidthMm, attributes);
        result *= INCH_IN_MM * attributes.getResolution().getVerticalDpi();
        return (int)result;
    }

    private int calculateGapMm(int columnWidthMm, PrintAttributes attributes) {
        int workingWidthMils =
                attributes.getMediaSize().getWidthMils()
                        - attributes.getMinMargins().getLeftMils()
                        - attributes.getMinMargins().getRightMils();
        int colsNum = (int)(workingWidthMils * MM_IN_MILS / (float) columnWidthMm);
        int result = (int)((workingWidthMils * MM_IN_MILS - columnWidthMm * colsNum) / (colsNum - 1));
        return result;
    }


    /**
     * Calculates gap in mm for given media and column width
     * @param columnWidthMm - column width in mm
     * @param attributes - print attributes
     * @return - number of columns
     */
    public int calculateColsNum(int columnWidthMm, PrintAttributes attributes) {
        Log.v(getClass().getName(),
                String.format("calculateColsNum( columnWidthMm = %d, attributes) widthMils: %d " +
                                "leftMarginMils: %d rightMarginMils: %d",
                        columnWidthMm, attributes.getMediaSize().getWidthMils(),
                        attributes.getMinMargins().getLeftMils(),
                        attributes.getMinMargins().getRightMils()));
        int workingWidthMils =
                attributes.getMediaSize().getWidthMils()
                        - attributes.getMinMargins().getLeftMils()
                        - attributes.getMinMargins().getRightMils();
        int result = (int)(workingWidthMils * MM_IN_MILS / (float) columnWidthMm);
        Log.v(getClass().getName(), String.format("result cols num :%d", result));
        return result;
    }

    public void setOriginal(View view) {
        ((RadioGroup)findViewById(R.id.font_size)).check(R.id.font_size_full);
        zoom = 1.0f;
    }

    public void setThreeQuaters(View view) {
        ((RadioGroup)findViewById(R.id.font_size)).check(R.id.font_size_three_quarters);
        zoom = 0.75f;
    }

    public void setHalf(View view) {
        ((RadioGroup)findViewById(R.id.font_size)).check(R.id.font_size_half);
        zoom = 0.5f;
    }

    public void setQuater(View view) {
        ((RadioGroup)findViewById(R.id.font_size)).check(R.id.font_size_quarter);
        zoom = 0.25f;
    }


}
