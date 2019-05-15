//
// Created by Sergey Mikhno on 2019-05-13.
//

#ifndef FLOW_READER_PAGESEGMENTER_H
#define FLOW_READER_PAGESEGMENTER_H

#include <flann/flann.hpp>
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
using namespace flann;
using namespace boost;

typedef adjacency_list < vecS, vecS, undirectedS > Graph;
typedef std::tuple<double,double> double_pair ;


struct glyph {
    int x, y, width, height;
};

struct line_limit {
    int upper, upper_baseline, lower, lower_baseline;
};


struct cc_result {
    int** centers;
    double  average_hight;
    map<double_pair,Rect> rd;
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
    vector<line_limit> get_line_limits();
    void preprocess_for_line_limits(const Mat &image);
    vector<cc_result> get_cc_results(const Mat& image);
    vector<std::tuple<int,int>> one_runs(const Mat& hist);
    vector<std::tuple<double,double>> get_connected_components();
    line_limit find_baselines(vector<double_pair>& cc);
};


#endif //FLOW_READER_PAGESEGMENTER_H
