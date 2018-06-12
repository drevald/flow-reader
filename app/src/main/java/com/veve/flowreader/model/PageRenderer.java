package com.veve.flowreader.model;

import android.graphics.Bitmap;

public interface PageRenderer {

    //todo - probably should return more than one bitmap to cope with 8000 pixel limit
    Bitmap renderPage(DevicePageContext context, int position);

    Bitmap renderOriginalPage(DevicePageContext context, int position);

}
