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
    }

    vector<glyph> get_glyphs();

private:
    Mat mat;
    vector<line_limit> get_line_limits();
    vector<cc_result> get_cc_results(Mat& image);
};


#endif //FLOW_READER_PAGESEGMENTER_H
