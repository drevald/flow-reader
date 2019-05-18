//
// Created by Sergey Mikhno on 2019-05-13.
//

#ifndef FLOW_READER_PAGESEGMENTER_H
#define FLOW_READER_PAGESEGMENTER_H

#include "flann/flann.hpp"
#include "flann/util/matrix.h"
#include <boost/graph/connected_components.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>
#include <tuple>
#include <map>

using namespace std;
using namespace cv;

using namespace boost;

typedef std::tuple<double,double> double_pair ;
typedef adjacency_list < vecS, vecS, undirectedS, double_pair > Graph;
typedef graph_traits<Graph>::vertex_descriptor vertex_t;


struct glyph {
    int x, y, width, height, line_height, baseline_shift;
};

struct line_limit {
    line_limit(int upper, int upper_baseline, int lower_baseline, int lower) {
        this->upper = upper;
        this->upper_baseline = upper_baseline;
        this->lower_baseline = lower_baseline;
        this->lower = lower;


    }
    int upper, upper_baseline, lower, lower_baseline;
};


struct cc_result {
    std::vector<double_pair> centers;
    double  average_hight;
};

struct PairXOrder {
    bool operator()(double_pair const &lhs, double_pair const &rhs) const {
        return get<0>(lhs) < get<0>(rhs);
    }
};

struct SortLineLimits {
    bool operator()(line_limit const &lhs, line_limit const &rhs) const {
        return lhs.lower_baseline < rhs.lower_baseline;
    }

};


class PageSegmenter {

public:
    PageSegmenter(Mat& mat) {
        this->mat = mat;
        cvtColor(mat, gray_inverted_image, COLOR_BGR2GRAY);
        bitwise_not(gray_inverted_image,gray_inverted_image);
    }

    vector<glyph> get_glyphs();

private:
    Mat mat;
    Mat gray_inverted_image;
    map<double_pair,int> center_numbers;
    map<double_pair,Rect> rd;
    Graph g;
    int line_height = 0;
    vector<line_limit> get_line_limits();
    void preprocess_for_line_limits(const Mat &image);
    cc_result get_cc_results(const Mat& image);
    vector<std::tuple<int,int>> one_runs(const Mat& hist);
    map<int,vector<double_pair>> get_connected_components(vector<double_pair>& center_list, double averahe_hight);
    line_limit find_baselines(vector<double_pair>& cc);
};


#endif //FLOW_READER_PAGESEGMENTER_H
