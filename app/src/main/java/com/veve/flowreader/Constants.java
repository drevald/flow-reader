package com.veve.flowreader;

import android.graphics.Color;

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
    String POSITION = "POSITION";
    String FILE_NAME = "FILENAME";

    int MARGIN_STEP = 50;
    int MARGIN_MAX = 250;

    float ZOOM_STEP = 0.5F;
    float ZOOM_MIN = 1.5F;
    float ZOOM_MAX = 5F;

    String PREFERENCES = "PREFERENCES";
    String FLOW_BOOK_PREFERENCES = "FLOW_BOOK_PREFERENCES";
    String SHOW_TRY_REFLOW = "SHOW_TRY_REFLOW";

    String KINDLE_NAVIGATION = "KINDLE_NAVIGATION";
    String PINCH_ZOOM = "PINCH_ZOOM";
    String SHOW_SCROLLBARS = "SHOW_SCROLLBARS";
    String JUSTIFY_TEXT = "JUSTIFY_TEXT";


}
