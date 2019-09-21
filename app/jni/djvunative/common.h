#ifndef FLOW_READER_COMMON_H
#define FLOW_READER_COMMON_H

#include <jni.h>

#include <android/log.h>
#include <time.h>
#include <string>
#include <stdio.h>
#include <errno.h>
#include <math.h>
#include <limits>
#include <cstdlib>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>

#include <boost/range/algorithm/copy.hpp>
#include <boost/range/adaptor/copied.hpp>
#include <boost/range/adaptor/map.hpp>
#include <boost/graph/connected_components.hpp>
#include <boost/graph/adjacency_list.hpp>

#include "flann/flann.hpp"
#include "flann/util/matrix.h"

#include <vector>
#include <tuple>
#include <map>
#include <vector>
#include <array>
#include <set>
#include <iostream>
#include <algorithm>
#include <stack>

#include <libdjvu/ddjvuapi.h>
#include <fpdfview.h>

#define APPNAME "FLOW-READER"

using namespace cv;
using namespace std;

using namespace boost;

struct segment {
    int y, height;
};

struct glyph {
    bool indented;
    int x, y, width, height, line_height, baseline_shift;
};


struct SortSegments {
    bool operator()(std::tuple<int,char> const &lhs, std::tuple<int,char> const &rhs) {
        return get<0>(lhs) < get<0>(rhs);
    }
};

void put_glyphs(JNIEnv *env, vector<glyph>& glyphs, jobject& list);

double TimeSpecToSeconds(struct timespec* ts);

std::vector<std::tuple<int,int>> zero_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs_vert(const Mat &hist);

std::vector<std::tuple<int,int>> zero_runs_hor(const cv::Mat& hist);

float calcMHWScore(vector<int> scores);

bool well_formed_page(Mat& image);

bool build_well_formed_page(Mat& image, Mat& gray_inverted_image);

void filter_gray_inverted_image(std::vector<segment> segments, int width, int height, Mat& gray_inverted_image);

int max_ind(std::vector<std::tuple<int,int>> zr);


#endif
