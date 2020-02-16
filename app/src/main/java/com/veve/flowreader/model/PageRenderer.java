package com.veve.flowreader.model;

import android.graphics.Bitmap;

import java.util.List;

public interface PageRenderer {

    List<Bitmap> renderPage(DevicePageContext context, int position);

    Bitmap renderOriginalPage(DevicePageContext context, int position);

    Bitmap renderOriginalPage(int position);

    void setPageLayoutParser(PageLayoutParser parser);

    void closeBook();

}
