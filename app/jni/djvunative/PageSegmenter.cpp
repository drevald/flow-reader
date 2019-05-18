//
// Created by Sergey Mikhno on 2019-05-13.
//

#include "PageSegmenter.h"
#include "Enclosure.h"

#include <opencv2/opencv.hpp>

#include <limits>
#include <cstdlib>

#include <boost/range/algorithm/copy.hpp>
#include <boost/range/adaptor/copied.hpp>
#include <boost/range/adaptor/map.hpp>


static int *calc_histogram(double *data, int size, double min, double max, int numBins) {
    int *result = new int[numBins];
    const double binSize = (max - min) / numBins;

    for (int i = 0; i < size; i++) {
        double d = data[i];
        int bin = (int) ((d - min) / binSize);
        if (bin < 0) { /* this data is smaller than min */ }
        else if (bin >= numBins) { /* this data point is bigger than max */ }
        else {
            result[bin] += 1;
        }
    }
    return result;
}

line_limit PageSegmenter::find_baselines(vector<double_pair> &cc) {

    sort(cc.begin(), cc.end(), PairXOrder());
    vector<Rect> line_rects;

    for (int i = 0; i < cc.size(); i++) {
        double_pair t = cc.at(i);
        line_rects.push_back(rd.at(t));
    }

    int max = numeric_limits<int>::min();
    int min = numeric_limits<int>::max();

    double upperData[line_rects.size()];
    double lowerData[line_rects.size()];

    for (int i = 0; i < line_rects.size(); i++) {
        Rect rect = line_rects.at(i);
        if (rect.y < min) {
            min = rect.y;
        }
        if (rect.y + rect.height > max) {
            max = rect.y + rect.height;
        }
        upperData[i] = rect.y;
        lowerData[i] = rect.y + rect.height;
    }

    int size = line_rects.size();

    Mat ud(size, 1, CV_64F, &upperData);
    Mat ld(size, 1, CV_64F, &lowerData);

    map<double,int> mycounts1;
    map<double,int> mycounts2;

    std::vector<int> counts1(50, 0);
    for (int c = 0; c < size; c++) {
        double n = ud.at<double>(c,0);
        if (mycounts1.find(n) == mycounts1.end()){
            mycounts1.insert(make_pair(n, 1));
        } else {
            mycounts1.at(n) +=1;
        }

        double m = ld.at<double>(c,0);
        if (mycounts2.find(m) == mycounts2.end()){
            mycounts2.insert(make_pair(m, 1));
        } else {
            mycounts2.at(m) +=1;
        }

    }

    int maxUpper = numeric_limits<int>::min();
    int maxLower = numeric_limits<int>::min();


    int maxUpperIndex = 0;
    int maxLowerIndex = 0;

    for ( auto it = mycounts1.begin(); it != mycounts1.end(); it++ )
    {
        if (it->second > maxUpper) {
            maxUpper = it->second;
            maxUpperIndex = it->first;
        }
    }


    for ( auto it = mycounts2.begin(); it != mycounts2.end(); it++ )
    {
        if (it->second > maxLower) {
            maxLower = it->second;
            maxLowerIndex = it->first;
        }
    }

    ud.release();
    ld.release();

    return line_limit(min, maxUpperIndex, maxLowerIndex, max);

}


void PageSegmenter::preprocess_for_line_limits(const Mat &image) {
    threshold(image, image, 0, 255, THRESH_OTSU | THRESH_BINARY);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    erode(image, image, kernel, Point(-1, -1), 2);
    dilate(image, image, kernel, Point(-1, -1), 2);
}

map<int, vector<double_pair>>
PageSegmenter::get_connected_components(vector<double_pair> &center_list, double average_height) {

    map<int, vector<double_pair>> return_value;

    int size = center_list.size();
    double data[size][2];

    for (int i = 0; i < size; i++) {
        data[i][0] = get<0>(center_list.at(i));
        data[i][1] = get<1>(center_list.at(i));
    }

    ::flann::Matrix<double> dataset(&data[0][0], size, 2);

    int k = 30;

    ::flann::Index<::flann::L2<double>> index(dataset, ::flann::KDTreeIndexParams(1));
    index.buildIndex();

    ::flann::Matrix<int> indices(new int[size*k], size, k);
    ::flann::Matrix<double> dists(new double[size*k], size, k);

    double q[size][2];

    for (int i = 0; i < size; i++) {
        auto p = center_list[i];
        q[i][0] = get<0>(p);
        q[i][1] = get<1>(p);
    }

    ::flann::Matrix<double> query(&q[0][0], size, 2);
    index.knnSearch(query, indices, dists, 30, ::flann::SearchParams());

    map<int,bool> verts;

    for (int i = 0; i < size; i++) {
        auto p = center_list[i];

        vector<double_pair> neighbors;
        bool found_neighbor = false;
        double_pair right_nb;

        double mindist = numeric_limits<double>::max();

        for (int j = 0; j < k; j++) {
            int ind = indices[i][j];
            double_pair nb = center_list[ind];
            if (get<0>(nb) - get<0>(p) != 0) {
                double dist = ((get<1>(nb) - get<1>(p)) * (get<1>(nb) - get<1>(p))) /
                              (get<0>(nb) - get<0>(p)) + (get<0>(nb) - get<0>(p));
                if (dist < mindist && get<0>(nb) > get<0>(p) &&
                    abs((get<1>(nb) - get<1>(p))) < 3. / 4. * average_height) {
                    mindist = dist;
                    right_nb = make_tuple(get<0>(nb), get<1>(nb));
                    found_neighbor = true;
                }
            }
        }

        if (found_neighbor) {
            double_pair point(get<0>(p), get<1>(p));

            int n = center_numbers.at(point);
            int m = center_numbers.at(right_nb);

            vertex_t v1 = vertex(n, g);
            vertex_t v2 = vertex(m, g);
            add_edge(v1, v2, g);

        }

    }

    std::vector<int> c(num_vertices(g));

    int num = connected_components
            (g, make_iterator_property_map(c.begin(), get(vertex_index, g), c[0]));

    for (int i = 0; i < c.size(); i++) {
        int cn = c[i];
        if (return_value.find(cn) == return_value.end()) {
            return_value.insert(make_pair(cn, vector<double_pair>()));
        }
        return_value.at(cn).push_back(g[i]);

    }

    return return_value;
}


vector<line_limit> PageSegmenter::get_line_limits() {

    const Mat &image = gray_inverted_image.clone();
    preprocess_for_line_limits(image);


    const cc_result cc_results = get_cc_results(image);
    double average_height = cc_results.average_hight;
    vector<double_pair> centers = cc_results.centers;

    const map<int, vector<double_pair>> components = get_connected_components(centers, average_height);

    line_height = (int) average_height * 2;

    vector<int> keys;

    boost::copy(components | boost::adaptors::map_keys,
                std::back_inserter(keys));

    vector<line_limit> v;
    for (int i=0;i<keys.size(); i++) {
        int cn = keys.at(i);
        vector<double_pair> cc = components.at(cn);
        line_limit ll = find_baselines(cc);
        v.push_back(ll);
    }

    return v;
}

cc_result PageSegmenter::get_cc_results(const Mat &image) {

    Mat labeled(image.size(), image.type());
    Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    Mat centComponents = Mat::zeros(Size(0, 0), 0);
    connectedComponentsWithStats(image, labeled, rectComponents, centComponents);

    int count = rectComponents.rows - 1;
    double heights[count];

    vector<double_pair> center_list;
    vector<Rect> rects;

    for (int i = 1; i < rectComponents.rows; i++) {
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect rectangle(x, y, w, h);
        rects.push_back(rectangle);
        heights[i - 1] = h;

        double cx = centComponents.at<double>(i, 0);
        double cy = centComponents.at<double>(i, 1);

    }

    Scalar m, stdv;
    Mat hist(1, count, CV_64F, &heights);
    meanStdDev(hist, m, stdv);

    double average_height = m(0);
    double std = stdv(0);

    Enclosure enc(rects);
    const set<array<int, 4>> &s = enc.solve();


    int i = 0;
    for (auto it = s.begin(); it != s.end(); ++it) {
        array<int, 4> a = *it;
        int x = -get<0>(a);
        int y = -get<1>(a);
        int width = get<2>(a) - x;
        int height = get<3>(a) - y;
        if (height > average_height - std && height < 2*average_height) {
            double cx = (x + width) / 2.0;
            double cy = (y + height) / 2.0;
            rd[make_tuple((x + width) / 2.0, (y + height) / 2.0)] = Rect(x, y, width, height);
            double_pair center = make_pair(cx, cy);
            center_list.push_back(center);
            center_numbers.insert(make_pair(center, i));
            vertex_t v = add_vertex(g);
            g[v] = center;
            i++;
        }
    }

    cc_result v;
    v.average_hight = average_height;
    v.centers = center_list;
    return v;

}


vector<std::tuple<int, int>> PageSegmenter::one_runs(const Mat &hist) {
    int w = hist.cols;

    vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < w; i++) {
        if ((i == 0 && hist.at<int>(0, i) == 1) ||
            (i > 0 && hist.at<int>(0, i) == 1 && hist.at<int>(0, i - 1) == 0)) {
            pos = i;
        }

        if ((i == w - 1 && hist.at<int>(0, i) == 1) ||
            (i < w - 1 && hist.at<int>(0, i) == 1 && hist.at<int>(0, i + 1) == 0)) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}


vector<glyph> PageSegmenter::get_glyphs() {

    // preprocess for the first step
    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image, image);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    dilate(image, image, kernel, Point(-1, -1), 2);

    vector<line_limit> line_limits = get_line_limits();

    sort(line_limits.begin(), line_limits.end(), SortLineLimits());

    vector<glyph> return_value;

    int width = image.cols;
    for (line_limit &ll : line_limits) {
        int l = ll.lower;
        int bl = ll.lower_baseline;
        int u = ll.upper;
        int bu = ll.upper_baseline;

        Mat lineimage(image, Rect(0, u, width, l - u));
        threshold(lineimage, lineimage, 0, 255, THRESH_BINARY | THRESH_OTSU);
        Mat horHist;
        reduce(lineimage, horHist, 0, REDUCE_SUM, CV_32F);

        int w = horHist.cols;

        for (int i = 0; i < w; i++) {
            if (horHist.at<int>(0, i) > 0) {
                horHist.at<int>(0, i) = 1;
            } else {
                horHist.at<int>(0, i) = 0;
            }
        }

        const vector<std::tuple<int, int>> &oneRuns = one_runs(horHist);

        horHist.release();

        for (const std::tuple<int, int> &r : oneRuns) {

            int left = get<0>(r);
            int right = get<1>(r);

            glyph g;
            g.x = left;
            g.y = u;
            g.width = right - left;
            g.height = l - u;
            g.baseline_shift = l - bl;
            g.line_height = line_height;
            return_value.push_back(g);

        }

    }

    return return_value;
}

