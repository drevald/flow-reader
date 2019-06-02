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

#include <libdjvu/ddjvuapi.h>
#include <fpdfview.h>

#include "PageSegmenter.h"

#define APPNAME "FLOW-READER"

using namespace cv;
using namespace std;

using namespace boost;


void put_glyphs(JNIEnv *env, Mat& mat, jobject& list);

double TimeSpecToSeconds(struct timespec* ts);

std::vector<std::tuple<int,int>> zero_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs(const Mat& hist);

std::vector<std::tuple<int, int>> one_runs_vert(const Mat &hist);

float calcMHWScore(vector<int> scores);

bool well_formed_page(Mat& image);
