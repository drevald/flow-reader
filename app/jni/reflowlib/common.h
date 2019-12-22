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
#include <stdlib.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>
#include <opencv2/photo.hpp>

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
#include <wchar.h>
#include <codecvt>

#include <libdjvu/ddjvuapi.h>
#include <libdjvu/miniexp.h>
#include <fpdfview.h>
#include <fpdfdoc.h>


#define APPNAME "FLOW-READER"

using namespace cv;
using namespace std;

using namespace boost;

typedef struct segment_struct {
    int y, height;
} segment;

typedef struct glyph_struct {
    bool indented;
    int x, y, width, height, line_height, baseline_shift;
    int is_space = 0;
    int is_last = 0;
    int is_picture = 0;
} glyph;

struct image_format {
    int w;
    int h;
    int size;
public:
    image_format(int w, int h, int size) : w(w), h(h), size(size) {}
};

struct SortSegments {
    bool operator()(std::tuple<int,char> const &lhs, std::tuple<int,char> const &rhs) {
        return get<0>(lhs) < get<0>(rhs);
    }
};


std::pair<std::vector<int>,std::vector<float>> make_hist(std::vector<int>& v, int num_buckets, int min, int max);

double deviation(vector<int> v, double ave);

std::vector<glyph> preprocess(cv::Mat& image, cv::Mat& rotated_with_pictures);

std::vector<glyph> convert_java_glyphs(JNIEnv *env, jobject list);

void reflow(cv::Mat& cvMat, cv::Mat& new_image, float scale, bool portrait, JNIEnv* env, std::vector<glyph> glyphs, jobject list, std::vector<glyph> pic_glyphs, cv::Mat rotated_with_pictures, bool preprocessing, float margin);

void put_glyphs(JNIEnv *env, vector<glyph>& glyphs, jobject& list);

std::vector<std::tuple<int,int>> zero_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs_vert(const Mat &hist);

std::vector<std::tuple<int,int>> zero_runs_hor(const cv::Mat& hist);

int max_ind(std::vector<std::tuple<int,int>> zr);

int strlen16(char16_t* strarg);

std::vector<glyph> get_glyphs(cv::Mat mat);


#endif
