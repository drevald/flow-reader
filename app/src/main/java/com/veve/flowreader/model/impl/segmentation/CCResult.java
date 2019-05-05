package com.veve.flowreader.model.impl.segmentation;

import com.veve.flowreader.algorithm.Tuple;

import org.opencv.core.Rect;

import java.util.Map;

public class CCResult {

    private double[][] centers;

    private double averageHeight;

    private Map<Tuple<Double,Double>, Rect> reverseDictionary;

    public CCResult(double[][] centers, double averageHeight, Map<Tuple<Double, Double>, Rect> reverseDictionary) {
        this.centers = centers;
        this.averageHeight = averageHeight;
        this.reverseDictionary = reverseDictionary;
    }

    public double[][] getCenters() {
        return centers;
    }

    public double getAverageHeight() {
        return averageHeight;
    }

    public Map<Tuple<Double, Double>, Rect> getReverseDictionary() {
        return reverseDictionary;
    }
}
