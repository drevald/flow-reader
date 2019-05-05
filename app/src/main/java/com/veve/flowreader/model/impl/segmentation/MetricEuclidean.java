package com.veve.flowreader.model.impl.segmentation;

import flann.exception.ExceptionFLANN;
import flann.metric.Metric;

public class MetricEuclidean implements Metric {
    @Override
    public double distance(double[] a, double[] b) {
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += distance(a[i], b[i]);
        }
        return Math.sqrt(result);
    }

    @Override
    public double distance(double a, double b) {
        double diff = a - b;
        return Math.abs(diff);
    }

    @Override
    public int distance(int[] a, int[] b) {
        throw new ExceptionFLANN("Unsupported types");
    }

    @Override
    public int distance(int a, int b) {
        throw new ExceptionFLANN("Unsupported types");
    }
}
