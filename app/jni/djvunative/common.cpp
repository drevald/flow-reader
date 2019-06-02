#include "common.h"

void put_glyphs(JNIEnv *env, Mat& mat, jobject& list) {
    PageSegmenter ps(mat);
    vector<glyph> glyphs = ps.get_glyphs();

    jclass listcls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    jmethodID addMethod = env->GetMethodID(listcls, "add", "(Ljava/lang/Object;)Z");
    jclass clz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/veve/flowreader/model/PageGlyphInfo")));

    if (clz == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "clz is NULL");
    }

    //jclass clz = env->FindClass("com/veve/flowreader/model/PageGlyphInfo");
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(IIIIII)V");

    if (constructor == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "constructor is NULL");
    }


    for (glyph g : glyphs) {

        jobject object = env->NewObject(clz, constructor, g.x, g.y, g.width, g.height, g.line_height, g.baseline_shift);

        env->CallBooleanMethod(
                list,
                addMethod,
                object);

        env->DeleteLocalRef(object);

    }

    char msg[30];
    sprintf(msg, "glyphs count %d\n", glyphs.size());

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);
}

double TimeSpecToSeconds(struct timespec* ts) {
    return (double)ts->tv_sec + (double)ts->tv_nsec / 1000000000.0;
}

std::vector<std::tuple<int, int>> one_runs(const Mat &hist) {
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

std::vector<std::tuple<int, int>> one_runs_vert(const Mat &hist) {
    int h = hist.rows;

    vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < h; i++) {
        if ((i == 0 && hist.at<float>(i,0) == 1) ||
            (i > 0 && hist.at<float>(i,0) == 1 && hist.at<float>(i - 1,0) == 0)) {
            pos = i;
        }

        if ((i == h - 1 && hist.at<float>(i,0) == 1) ||
            (i < h - 1 && hist.at<float>(i,0) == 1 && hist.at<float>(i + 1,0) == 0)) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}


std::vector<std::tuple<int,int>> zero_runs(const Mat& hist) {
    int w = hist.rows;

    vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < w; i++) {
        if ((i == 0 && hist.at<float>(i, 0) == 0) ||
            (i > 0 && hist.at<float>(i, 0) == 0 && hist.at<float>(i - 1,0) == 1)) {
            pos = i;
        }

        if ((i == w - 1 && hist.at<float>(i, 0) == 0) ||
            (i < w - 1 && hist.at<float>(i, 0) == 0 && hist.at<float>(i + 1,0) == 1)) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}

float calcMHWScore(vector<int> scores)
{
    size_t size = scores.size();

    if (size == 0)
    {
        return 0;  // Undefined, really.
    }
    else
    {
        sort(scores.begin(), scores.end());
        if (size % 2 == 0)
        {
            return (scores[size / 2 - 1] + scores[size / 2]) / 2;
        }
        else
        {
            return scores[size / 2];
        }
    }
}

bool well_formed_page(Mat& image) {
    Mat vertHist;
    Mat thr;

    threshold(image, thr, 0, 255, THRESH_BINARY | THRESH_OTSU);

    reduce(thr, vertHist, 1, REDUCE_SUM, CV_32F);

    int h = vertHist.rows;

    for (int i = 0; i < h; i++) {
        if (vertHist.at<float>(i, 0) > 0) {
            vertHist.at<float>(i, 0) = 1;
        } else {
            vertHist.at<float>(i, 0) = 0;
        }
    }

    std::vector<std::tuple<int,int>> zeroRuns = zero_runs(vertHist);

    std::vector<std::tuple<int,int>> oneRuns = one_runs_vert(vertHist);


    char msg[50];
    sprintf(msg, "zero runs count %d\n", zeroRuns.size());
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);


    sprintf(msg, "one runs count %d\n", oneRuns.size());
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);

    if ((oneRuns.size() < 2) || (zeroRuns.size() < 3)) {
        vertHist.release();
        thr.release();
        return false;
    }

    std::vector<int> heights;

    for (int i=0;i<oneRuns.size(); i++) {
        std::tuple<int,int> one_run = oneRuns[i];
        heights.push_back(get<1>(one_run) - get<0>(one_run));
    }

    float median_height = calcMHWScore(heights);

    std::vector<int> filtered_heights;

    for (int i=0;i<heights.size(); i++) {
        if (heights[i] < 1.5*median_height) {
            filtered_heights.push_back(heights[i]);
        }
    }
    //std::copy_if (heights.begin(), heights.end(), std::back_inserter(filtered_heights), [heights,median_height](int i){return heights[i] < 1.5*median_height;} );

    double sum = std::accumulate(filtered_heights.begin(), filtered_heights.end(), 0.0);
    double mean = sum / filtered_heights.size();

    double sq_sum = std::inner_product(filtered_heights.begin(), filtered_heights.end(), filtered_heights.begin(), 0.0);
    double stdev = std::sqrt(sq_sum / filtered_heights.size() - mean * mean);


    bool lined_document = true;

    for (int i=0;i<filtered_heights.size(); i++) {
        int h = filtered_heights[i];
        if (h > mean + 6*stdev) {
            sprintf(msg, "h mean stddev median %d %f %f %f \n", h, mean, stdev, median_height);
            __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);
            lined_document = false;
            break;
        }
    }

    thr.release();
    vertHist.release();

    if (lined_document) {
        sprintf(msg, "true\n");
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);
    } else {
        sprintf(msg, "false\n");
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", msg);
    }

    return lined_document;

}
