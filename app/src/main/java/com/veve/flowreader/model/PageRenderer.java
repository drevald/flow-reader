package com.veve.flowreader.model;

import android.graphics.Bitmap;

public interface PageRenderer {

    Bitmap renderPage(DevicePageContext context, int position, boolean preprocessing , boolean invalidateCache);

    Bitmap renderPage(DevicePageContext context, int position);

    Bitmap renderOriginalPage(DevicePageContext context, int position);

    Bitmap renderOriginalPage(int position);

    void setPageLayoutParser(PageLayoutParser parser);

}
