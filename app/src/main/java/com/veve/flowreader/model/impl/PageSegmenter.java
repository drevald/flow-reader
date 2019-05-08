package com.veve.flowreader.model.impl;


import android.graphics.Bitmap;
import android.util.Log;

import com.veve.flowreader.algorithm.Enclosure;
import com.veve.flowreader.algorithm.Tuple;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.impl.segmentation.CCResult;
import com.veve.flowreader.model.impl.segmentation.LineLimit;
import com.veve.flowreader.model.impl.segmentation.MetricEuclidean;

import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flann.index.IndexBase;
import flann.index.IndexKDTree;
import flann.index.IndexKMeans;
import flann.metric.Metric;
import flann.metric.MetricEuclideanSquared;

public class PageSegmenter implements PageLayoutParser {

    private Mat mat;

    private int lineHeight;

    public PageSegmenter() {

    }

    @Override
    public List<PageGlyph> getGlyphs(Bitmap bitmap) {

        int iBytes = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);
        byte[] imageBytes= buffer.array();
        bitmap.copyPixelsToBuffer(buffer);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        mat = new Mat(height, width , CvType.CV_8UC4);
        mat.put(0,0,imageBytes, 0, imageBytes.length);

        List<PageGlyph> returnValue = new ArrayList<>();

        Mat image = new Mat();
        Imgproc.cvtColor(mat,image, Imgproc.COLOR_BGR2GRAY);

        Core.bitwise_not(image,image);

        final Size kernelSize = new Size(8, 2);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
        Imgproc.dilate(image, image, kernel, new Point(0,0), 2);

        long start = System.currentTimeMillis();
        List<LineLimit> lineLimits = getLineLimits();


        for (LineLimit lineLimit : lineLimits) {
            int l = lineLimit.getLower();
            int bl = lineLimit.getLowerBaseline();
            int u = lineLimit.getUpper();
            int bu = lineLimit.getUpperBaseline();

            Mat lineimage = new Mat(image, new Rect(0, u, width, l-u));
            Imgproc.threshold(lineimage, lineimage,0,255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            Mat horHist = new Mat();
            Core.reduce(lineimage, horHist, 0, Core.REDUCE_SUM, CvType.CV_32F);

            int w = horHist.width();

            for (int i=0;i<w;i++) {
                if (horHist.get(0,i)[0] > 0) {
                    horHist.put(0,i, 1);
                } else {
                    horHist.put(0,i, 0);
                }
            }

            List<Tuple<Integer, Integer>> oneRuns = oneRuns(horHist);

            horHist.release();

            for (Tuple<Integer,Integer> r : oneRuns) {
                int left = r.getFirst();
                int right = r.getSecond();

                Mat glyph = new Mat(mat, new Rect(left, u, right-left, l-u));

                Bitmap newBitmap = Bitmap.createBitmap(right-left, l-u, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(glyph, newBitmap);
                glyph.release();
                returnValue.add(new PageGlyphImpl(newBitmap, l - bl, lineHeight));

            }
        }

        image.release();
        mat.release();
        Log.d("DURATION", "" + (System.currentTimeMillis() - start));

        return returnValue;
    }

    private List<LineLimit> getLineLimits() {

        Mat image = new Mat();
        Imgproc.cvtColor(mat,image, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_not(image,image);

        Imgproc.threshold(image, image,0,255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        final Size kernelSize = new Size(3, 3);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);

        Imgproc.erode(image, image, kernel, new Point(0,0), 2);
        Imgproc.dilate(image, image, kernel, new Point(0,0), 2);

        CCResult ccResult = getCCResults(image);
        double averageHeight = ccResult.getAverageHeight();
        lineHeight = (int)averageHeight * 2;
        double[][] centers = ccResult.getCenters();
        Map<Tuple<Double, Double>, Rect> reverseDictionary = ccResult.getReverseDictionary();

        List<Set<Tuple<Double, Double>>> connectedComponents = getConnectedComponents(averageHeight, centers);

        List<LineLimit> lineLimits = new ArrayList<>();

        for (Set<Tuple<Double, Double>> cc : connectedComponents) {
            LineLimit lineLimit = findBaselines(reverseDictionary, cc);
            lineLimits.add(lineLimit);
        }

        image.release();

        Collections.sort(lineLimits, new Comparator<LineLimit>() {
            @Override
            public int compare(LineLimit l1, LineLimit l2) {
                return Integer.compare(l1.getLowerBaseline(), l2.getLowerBaseline());
            }
        });

        return lineLimits;
    }

    private LineLimit findBaselines(Map<Tuple<Double, Double>, Rect> rd, Set<Tuple<Double, Double>> cc) {

        List<Tuple<Double, Double>> list = new ArrayList<>(cc);
        Collections.sort(list, new Comparator<Tuple<Double, Double>>() {
            @Override
            public int compare(Tuple<Double, Double> o1, Tuple<Double, Double> o2) {
                return Double.compare(o1.getFirst(), o2.getFirst());
            }
        });

        List<Rect> lineRects = new ArrayList<>();

        for (Tuple<Double, Double> tuple : list) {
            lineRects.add(rd.get(tuple));
        }

        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        double[] upperData = new double[lineRects.size()];
        double[] lowerData = new double[lineRects.size()];

        for (int i=0;i<lineRects.size(); i++) {
            Rect rect = lineRects.get(i);
            if (rect.y < min) {
                min = rect.y;
            }
            if (rect.y + rect.height > max) {
                max = rect.y + rect.height;
            }
            upperData[i] = rect.y;
            lowerData[i] = rect.y + rect.height;
        }

        int[] hist1 = calcHistogram(upperData,min,max,max-min);
        int[] hist2 = calcHistogram(lowerData,min,max,max-min);

        int maxPos = 0;
        int minPos = 0;

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MIN_VALUE;

        for (int i=0;i< hist1.length; i++) {
            if (hist1[i] > maxValue) {
                maxPos = i;
                maxValue = hist1[i];
            }
        }

        for (int i=0;i< hist2.length; i++) {
            if (hist2[i] > minValue) {
                minPos = i;
                minValue = hist2[i];
            }
        }
        LineLimit lineLimit = new LineLimit(min, min+maxPos, min+minPos, max);

        return lineLimit;
    }


    private static List<Tuple<Integer,Integer>> oneRuns(Mat hist) {
        int w = hist.width();
        List<Tuple<Integer,Integer>> returnValue = new ArrayList<>();
        int pos = 0;
        for (int i=0;i<w;i++) {
            if ((i==0 && hist.get(0,i)[0] == 1) || (i> 0 && hist.get(0,i)[0] == 1 && hist.get(0,i-1)[0] == 0)) {
                pos = i;
            }

            if ((i==w-1 && hist.get(0,i)[0] == 1) || (i<w-1 && hist.get(0,i)[0] == 1 && hist.get(0,i+1)[0] == 0 ) ) {
                returnValue.add(new Tuple<>(pos, i));
            }
        }
        return returnValue;
    }


    private static int[] calcHistogram(double[] data, double min, double max, int numBins) {
        final int[] result = new int[numBins];
        final double binSize = (max - min)/numBins;

        for (double d : data) {
            int bin = (int) ((d - min) / binSize);
            if (bin < 0) { /* this data is smaller than min */ }
            else if (bin >= numBins) { /* this data point is bigger than max */ }
            else {
                result[bin] += 1;
            }
        }
        return result;
    }


    private List<Set<Tuple<Double, Double>>> getConnectedComponents(double averageHeight, double[][] centers) {

        Metric metric = new MetricEuclidean();

        IndexKDTree.BuildParams buildParams = new IndexKDTree.BuildParams(1);
        IndexBase index = new IndexKDTree(metric, centers, buildParams);
        index.buildIndex();

        EdgeFactory<Tuple<Double,Double>, DefaultEdge> edgeFactory = new ClassBasedEdgeFactory<Tuple<Double,Double>, DefaultEdge>(DefaultEdge.class);
        UndirectedGraph<Tuple<Double,Double>,DefaultEdge> graph = new SimpleGraph<Tuple<Double,Double>, DefaultEdge>(edgeFactory);

        int n = centers.length;

        int k = 30;

        double[][] queries = new double[n][2];

        int[][] indices = new int[n][k];
        double[][] distances = new double[n][k];

        IndexKMeans.SearchParams searchParams = new IndexKMeans.SearchParams();
        searchParams.maxNeighbors = k;
        searchParams.eps = 0.0f;

        for (int i=0; i<n; i++) {
            double[] p = centers[i];
            queries[i][0] = p[0];
            queries[i][1] = p[1];
        }

        index.knnSearch(queries, indices, distances, searchParams);

        for (int i=0; i<n; i++) {
            double[] p = centers[i];
            List<Tuple<Double,Double>> neighbors = new ArrayList<>();

            Tuple<Double,Double> rightNb = null;
            double mindist = Double.MAX_VALUE;

            for (int j = 0; j < k; j++) {
                int ind = indices[i][j];
                double[] nb = centers[ind];
                if (nb[0] - p[0] != 0) {
                    double dist = ((nb[1] - p[1]) * (nb[1] - p[1])) / (nb[0] - p[0]) + (nb[0] - p[0]);
                    if (dist < mindist && nb[0] > p[0] && Math.abs((nb[1] - p[1])) < 3. / 4. * averageHeight) {
                        mindist = dist;
                        rightNb = new Tuple<>(nb[0], nb[1]);
                    }
                }
            }

            if (rightNb != null) {
                Tuple<Double,Double> point = new Tuple<>(p[0], p[1]);
                graph.addVertex(point);
                graph.addVertex(rightNb);
                graph.addEdge(point, rightNb);
            }

        }

        ConnectivityInspector<Tuple<Double,Double>,DefaultEdge> inspector
                = new ConnectivityInspector<Tuple<Double,Double>,DefaultEdge>(graph);

        return inspector.connectedSets();

    }

    private CCResult getCCResults(Mat image) {

        Map<Tuple<Double, Double>, Rect> rd = new HashMap<>();

        Mat labeled = new Mat(image.size(), image.type());

        // Extract components
        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);
        Imgproc.connectedComponentsWithStats(image, labeled, rectComponents, centComponents);


        int[] rectangleInfo = new int[5];
        double[] centroidInfo = new double[2];

        int count = rectComponents.rows() - 1;
        double[] heights = new double[count];

        List<Tuple<Double,Double>> centerList = new ArrayList<>();

        List<Rect> rects = new ArrayList<>();
        for(int i = 1; i < rectComponents.rows(); i++) {

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            heights[i-1] = rectangleInfo[3];
            rects.add(rectangle);

            // Extract centroids
            centComponents.row(i).get(0, 0, centroidInfo);
            //  Point centroid = new Point(centroidInfo[0], centroidInfo[1]);

            //regions[i - 1] = new Region(rectangle, centroid);

        }

        // Free memory
        rectComponents.release();
        centComponents.release();
        labeled.release();

        MatOfDouble h = new MatOfDouble(heights);

        MatOfDouble stdsrc= new MatOfDouble();
        MatOfDouble meansrc= new MatOfDouble();

        Core.meanStdDev(h, meansrc, stdsrc);

        double averageHeight = meansrc.get(0, 0)[0];
        double stddev = stdsrc.get(0, 0)[0];

        Enclosure enc = new Enclosure(rects);
        Set<Rect> rectSet = enc.find();


        for (Rect rect : rectSet) {
            int x1 = rect.x;
            int y1 = rect.y;

            if (rect.height > averageHeight - stddev && rect.height < 2*averageHeight) {
                centerList.add(new Tuple<>(x1 + rect.width/2.0, y1 + rect.height/2.0 ));
                rd.put(new Tuple<>(x1 + rect.width/2.0, y1 + rect.height/2.0), rect);
            }
        }

        double[][] centers = new double[centerList.size()][2];

        for (int i=0;i<centerList.size(); i++) {
            Tuple<Double, Double> tuple = centerList.get(i);
            centers[i] = new double[] {tuple.getFirst(), tuple.getSecond()};
        }

        return new CCResult(centers, averageHeight, rd);
    }


    private static PageLayoutParser parser;

    public static PageLayoutParser getInstance() {
        if (parser == null) {
            parser = new PageSegmenter();
        }
        return parser;
    }


}

