package com.veve.flowreader;

import android.graphics.Color;
import android.print.PrintAttributes;

/**
 * Created by ddreval on 15.01.2018.
 */

public interface Constants {

    int VIEW_MODE_PHONE = 1;
    int VIEW_MODE_ORIGINAL = 2;
    String VIEW_MODE_PARAM = "viewMode";

    int LIGHT_PINK = Color.rgb(255, 64, 129);
    int GREEN = Color.rgb(0, 64, 0);
    int BOOK_THUMB_WIDTH = 100;
    int BOOK_THUMB_HOR_PADDING = 20;
    String VIEW_TYPE = "VIEW_TYPE";
    int LIST_VIEW_TYPE = 0;
    int GRID_VIEW_TYPE = 1;
    boolean DEBUG = false;
    int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB
    int IMAGE_VIEW_HEIGHT_LIMIT = 4000;

    String BOOK_CONTEXT = "BOOK_CONTEXT";
    String BOOK_ID = "BOOK_ID";
    String REPORT_ID = "REPORT_ID";
    String POSITION = "POSITION";
    String FILE_NAME = "FILENAME";

    int MARGIN_STEP = 50;
    int MARGIN_MAX = 250;

    public final String REPORT_URL = "https://glyphs.flum.app/loader";

    float ZOOM_STEP = 0.25F;
    float ZOOM_MIN = 0.25F;
    float ZOOM_MAX = 5F;

    float MM_IN_MILS = 0.0254f;
    float MM_IN_INCH = 25.4f;
    float INCH_IN_MILS = 0.001f;
    float MILS_IN_MM = 39.3701f;
    float INCH_IN_MM = 0.0393701f;

    String PREFERENCES = "PREFERENCES";
    String FLOW_BOOK_PREFERENCES = "FLOW_BOOK_PREFERENCES";
    String SHOW_TRY_REFLOW = "SHOW_TRY_REFLOW";

    PrintAttributes.MediaSize DEFAULT_MEDIA_SIZE = PrintAttributes.MediaSize.ISO_A4;
    //PrintAttributes.MediaSize DEFAULT_MEDIA_SIZE = PrintAttributes.MediaSize.ISO_A6;

    String KINDLE_NAVIGATION = "KINDLE_NAVIGATION";


}
