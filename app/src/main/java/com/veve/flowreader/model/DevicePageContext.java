package com.veve.flowreader.model;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by ddreval on 16.01.2018.
 */

public class DevicePageContext implements Serializable  {

    private static final float DEFAULT_ZOOM = 1f;

    private static final float DEFAULT_FONT_SIZE = 24;

    private static final float DEFAULT_LEADING = 0.5f * DEFAULT_FONT_SIZE;

    private static final float DEFAULT_KERNING = 0.5f; // 0.1f * DEFAULT_FONT_SIZE;

    private static final int DEFAULT_MARGIN = 25;

    private static final int DEFAULT_DISPLAY_DPI = 72;

    private int startPointX;

    private int startPointY;

    private int remotestPointX;

    private int remotestPointY;

    private Canvas canvas;

    private float zoom;

    private float zoomOriginal;

    private int width;

    private int lineHeight;

    private float kerning;

    private float leading;

    private float margin;

    private int displayDpi;

    private int currentBaseLine;

    private boolean newline;

    private boolean preprocessing;

    private boolean invalidateCache;

    private boolean portrait;

    private float screenRatio;

    private int resolution;

    public DevicePageContext(int width) {
        this.zoom = DEFAULT_ZOOM;
        this.kerning = DEFAULT_KERNING;
        this.leading = DEFAULT_LEADING;
        this.width = width;
        this.margin = 1.0f;
        this.startPointX = (int)margin*DEFAULT_MARGIN;
        this.startPointY = 0;
        this.remotestPointX = (int)margin*DEFAULT_MARGIN;
        this.remotestPointY = 0;
        this.displayDpi = DEFAULT_DISPLAY_DPI;
        this.currentBaseLine = 0;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public boolean getPortrait() {
        return portrait;
    }

    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
    }

    public float getZoomOriginal() {return zoomOriginal;}

    public void setZoomOriginal(float originalZoom) {this.zoomOriginal = originalZoom;}

    public DevicePageContext() {
        newline = true;
    }

    public Point getStartPoint() {
        return new Point(startPointX, startPointY);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public int getWidth() {
        Log.v(getClass().getName(), String.format("Width for %s after update %d", hashCode(), this.width));
        return this.width;
    }

    public void setWidth(int width) {
        Log.v(getClass().getName(), String.format("Width for %s before update %d", hashCode(), this.width));
        this.width = width;
        Log.v(getClass().getName(), String.format("Width for %s after update %d", hashCode(), this.width));
    }

    public Point getRemotestPoint() {
        return new Point (remotestPointX, remotestPointY);
    }

    public float getKerning() {
        return kerning;
    }

    public void setKerning(float kerning) {
        this.kerning = kerning;
    }

    public float getLeading() {
        return leading;
    }

    public void setLeading(float leading) {
        this.leading = leading;
    }

    public float getMargin() {
        return margin;
    }

    public void setMargin(float margin) {
        this.margin = margin;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getCurrentBaseLine() {
        return currentBaseLine;
    }

    public void setCurrentBaseLine(int currentBaseLine) {
        this.currentBaseLine = currentBaseLine;
    }

    public boolean isNewline() {
        return newline;
    }

    public void setNewline(boolean newline) {
        this.newline = newline;
    }

    public boolean isPreprocessing() {
        return preprocessing;
    }

    public void setPreprocessing(boolean preprocessing) {
        this.preprocessing = preprocessing;
    }

    public boolean isInvalidateCache() {
        return invalidateCache;
    }

    public void setInvalidateCache(boolean invalidateCache) {
        this.invalidateCache = invalidateCache;
    }

    public float getScreenRatio() {
        return screenRatio;
    }

    public void setScreenRatio(float screenRatio) {
        this.screenRatio = screenRatio;
    }

    public void resetPosition() {
        this.startPointX = (int)margin*DEFAULT_MARGIN;
        this.startPointY = 0;
        this.remotestPointX = (int)margin*DEFAULT_MARGIN;
        this.remotestPointY = 0;
    }

}
