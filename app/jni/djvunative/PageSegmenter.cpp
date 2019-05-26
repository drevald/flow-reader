//
// Created by Sergey Mikhno on 2019-05-13.
//

#include "PageSegmenter.h"
#include "Enclosure.h"

#include <opencv2/opencv.hpp>

#include <limits>
#include <cstdlib>
#include <android/log.h>

#include <boost/range/algorithm/copy.hpp>
#include <boost/range/adaptor/copied.hpp>
#include <boost/range/adaptor/map.hpp>

static double TimeSpecToSeconds(struct timespec* ts)
{
    return (double)ts->tv_sec + (double)ts->tv_nsec / 1000000000.0;
}


struct SortByDist {

    SortByDist(double_pair& p) : p(p) {
    }

    bool operator()(const double_pair& lhs, const double_pair& rhs) {
        double x1 = get<0>(lhs);
        double y1 = get<1>(lhs);

        double x2 = get<0>(rhs);
        double y2 = get<1>(rhs);

        double d1 = (x1 - get<0>(p))*(x1 - get<0>(p)) +  (y1 - get<1>(p))*(y1 - get<1>(p));
        double d2 = (x2 - get<0>(p))*(x2 - get<0>(p)) +  (y2 - get<1>(p))*(y2 - get<1>(p));

        return d1 < d2;
    }

private:
    double_pair p;

};



line_limit PageSegmenter::find_baselines(vector<double_pair> &cc) {

    sort(cc.begin(), cc.end(), PairXOrder());
    vector<Rect> line_rects;

    for (int i = 0; i < cc.size(); i++) {
        double_pair t = cc.at(i);
        line_rects.push_back(rd.at(t));
    }

    int max = numeric_limits<int>::min();
    int min = numeric_limits<int>::max();

    double lowerData[line_rects.size()];

    for (int i = 0; i < line_rects.size(); i++) {
        Rect rect = line_rects.at(i);
        if (rect.y < min) {
            min = rect.y;
        }
        if (rect.y + rect.height > max) {
            max = rect.y + rect.height;
        }
        lowerData[i] = rect.y + rect.height;
    }

    int size = line_rects.size();

    map<double,int> countsLower;

    for (int c = 0; c < size; c++) {
        double m = lowerData[c];
        if (countsLower.find(m) == countsLower.end()){
            countsLower.insert(make_pair(m, 1));
        } else {
            countsLower.at(m) +=1;
        }

    }

    int maxLower = numeric_limits<int>::min();
    int maxLowerIndex = 0;

    for ( auto it = countsLower.begin(); it != countsLower.end(); it++ )
    {
        if (it->second > maxLower) {
            maxLower = it->second;
            maxLowerIndex = it->first;
        }
    }

    return line_limit(min, 0, maxLowerIndex, max);

}


void PageSegmenter::preprocess_for_line_limits(const Mat &image) {
    threshold(image, image, 0, 255, THRESH_OTSU | THRESH_BINARY);
    //const Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    //morphologyEx(image,image,MORPH_OPEN,kernel,Point(-1,-1), 2);
    //erode(image, image, kernel, Point(-1, -1), 2);
    //dilate(image, image, kernel, Point(-1, -1), 2);
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
    int k = std::min(100,size);

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
    index.knnSearch(query, indices, dists, k, ::flann::SearchParams());
    for (int i = 0; i < size; i++) {
        auto p = center_list[i];

        Rect r1 = rd.at(p);
        double upper = r1.y - r1.height/2.0;
        double lower = r1.y + r1.height + r1.height/2.0;

        vector<double_pair> right_nbs;
        for (int j = 0; j < k; j++) {
            int ind = indices[i][j];
            double_pair nb = center_list[ind];
            Rect r2 = rd.at(nb);

            if (r2.x > r1.x  &&  (r2.y > upper && r2.y + r2.height < lower)  ) {
                right_nbs.push_back(nb);
            }
        }

        sort(right_nbs.begin(), right_nbs.end(), SortByDist(p));


        if (right_nbs.size() > 0 ) {
            double_pair point(get<0>(p), get<1>(p));
            double_pair right_nb = right_nbs[0];
            Rect r3 = rd.at(p);

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

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;
    clock_gettime(CLOCK_MONOTONIC, &start);
    preprocess_for_line_limits(image);

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "pp duration%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, "DJVU1", "%s\n", duration);




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
            double_pair center = std::tuple<double,double>(cx, cy);
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

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;
    clock_gettime(CLOCK_MONOTONIC, &start);


    // preprocess for the first step
    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image, image);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    dilate(image, image, kernel, Point(-1, -1), 2);


    vector<line_limit> line_limits = get_line_limits();

    sort(line_limits.begin(), line_limits.end(), SortLineLimits());

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "total duration%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, "DJVU1", "%s\n", duration);

    vector<glyph> return_value;

    int width = image.cols;
    Mat horHist;

    threshold(image, image, 0, 255, THRESH_BINARY | THRESH_OTSU);

    for (line_limit &ll : line_limits) {
        int l = ll.lower;
        int bl = ll.lower_baseline;
        int u = ll.upper;
        int bu = ll.upper_baseline;

        Mat lineimage(image, Rect(0, u, width, l - u));
        //threshold(lineimage, lineimage, 0, 255, THRESH_BINARY | THRESH_OTSU);

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
    horHist.release();

    mat.release();
    image.release();

    return return_value;
}

