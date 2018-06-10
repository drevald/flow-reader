package com.veve.flowreader.model.impl.pdf;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.model.DevicePageContext;

public class PdfDevicePageContext implements DevicePageContext {

    private int displayDpi;

    @Override
    public Point getStartPoint() {
        return null;
    }

    @Override
    public void setStartPoint(Point point) {

    }

    @Override
    public Canvas getCanvas() {
        return null;
    }

    @Override
    public void setCanvas(Canvas canvas) {

    }

    @Override
    public float getZoom() {
        return 0;
    }

    @Override
    public void setZoom(float zoom) {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public Point getRemotestPoint() {
        return null;
    }

    @Override
    public void setRemotestPoint(Point point) {

    }

    @Override
    public void resetPosition() {

    }

    @Override
    public float getKerning() {
        return 0;
    }

    @Override
    public void setKerning(float kerning) {

    }

    @Override
    public float getLeading() {
        return 0;
    }

    @Override
    public void setLeading(float leading) {

    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    public void setMargin(int margin) {

    }

    @Override
    public int getDisplayDpi() {
        return displayDpi;
    }

    @Override
    public void setDisplayDpi(int displayDpi) {
        this.displayDpi = displayDpi;
    }

    @Override
    public void setCurrentBaseLine(int baseLine) {

    }

    @Override
    public int getCurrentBaseLine() {
        return 0;
    }

    @Override
    public int getLineHeight() {
        return 0;
    }

    @Override
    public void setLineHeight(int lineHeight) {

    }
}
