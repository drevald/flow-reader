package com.veve.flowreader.model.impl.segmentation;

public class LineLimit {

    private int upper;

    private int upperBaseline;

    private int lowerBaseline;

    private int lower;

    public LineLimit(int upper, int upperBaseline, int lowerBaseline, int lower) {
        this.upper = upper;
        this.upperBaseline = upperBaseline;
        this.lowerBaseline = lowerBaseline;
        this.lower = lower;
    }

    public int getUpper() {
        return upper;
    }

    public int getUpperBaseline() {
        return upperBaseline;
    }

    public int getLowerBaseline() {
        return lowerBaseline;
    }

    public int getLower() {
        return lower;
    }
}