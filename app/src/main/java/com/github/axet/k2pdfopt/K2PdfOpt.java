package com.github.axet.k2pdfopt;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Map;

public class K2PdfOpt {

    static {
        if (Config.natives) {
            System.loadLibrary("k2pdfoptjni");
            Config.natives = true;
        }
    }

    private long handle;

    public K2PdfOpt() {
    }

    public native void create(int w, int h, int dpi);

    public native void setFontSize(float f);

    public native float getFontSize();

    public native void setVerbose(boolean b);

    public native boolean getVerbose();

    public native void setShowMarkedSource(boolean b);

    public native boolean getShowMarkedSource();

    public native void setLeftToRight(boolean b);

    public native boolean getLeftToRight();

    public native void load(Bitmap bm);

    public native int getCount();

    public native Bitmap renderPage(int i);

    public native Map<Rect, Rect> getRectMaps(int page); // Map<Src, Dest>

    public native void close();

}
