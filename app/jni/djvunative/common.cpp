#include "common.h"


void put_glyphs(JNIEnv *env, vector<glyph>& glyphs, jobject& list) {

    jclass listcls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    jmethodID addMethod = env->GetMethodID(listcls, "add", "(Ljava/lang/Object;)Z");
    jclass clz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/veve/flowreader/model/PageGlyphInfo")));

    if (clz == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "clz is NULL");
    }

    //jclass clz = env->FindClass("com/veve/flowreader/model/PageGlyphInfo");
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(ZIIIIII)V");

    if (constructor == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "constructor is NULL");
    }


    for (glyph g : glyphs) {

        jobject object = env->NewObject(clz, constructor, g.indented, g.x, g.y, g.width, g.height, g.line_height, g.baseline_shift);

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

std::vector<std::tuple<int, int>> one_runs(const cv::Mat &hist) {
    int w = hist.cols;

    vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < w; i++) {
        if ((i == 0 && hist.at<int>(0, i) > 0) ||
            (i > 0 && hist.at<int>(0, i) > 0 && hist.at<int>(0, i - 1) == 0)) {
            pos = i;
        }

        if ((i == w - 1 && hist.at<int>(0, i) > 0) ||
            (i < w - 1 && hist.at<int>(0, i) > 0 && hist.at<int>(0, i + 1) == 0)) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}

std::vector<std::tuple<int, int>> one_runs_vert(const cv::Mat &hist) {
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

std::vector<std::tuple<int,int>> zero_runs_hor(const cv::Mat& hist) {
    int w = hist.cols;

    std::vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < w; i++) {
        if ((i == 0 && hist.at<float>(0, i) == 0) ||
            (i > 0 && hist.at<float>(0, i) == 0 && hist.at<float>(0,i - 1) > 0)) {
            pos = i;
        }

        if ((i == w - 1 && hist.at<float>(0,i) == 0) ||
            (i < w - 1 && hist.at<float>(0,i) == 0 && hist.at<float>(0,i + 1) > 0)) {
            return_value.push_back(std::make_tuple(pos, i));
        }
    }
    return return_value;
}

std::vector<std::tuple<int,int>> zero_runs(const cv::Mat& hist) {
    int w = hist.rows;

    vector<std::tuple<int, int>> return_value;

    int pos = 0;
    for (int i = 0; i < w; i++) {
        if ((i == 0 && hist.at<float>(i, 0) == 0) ||
            (i > 0 && hist.at<float>(i, 0) == 0 && hist.at<float>(i - 1,0) > 0)) {
            pos = i;
        }

        if ((i == w - 1 && hist.at<float>(i, 0) == 0) ||
            (i < w - 1 && hist.at<float>(i, 0) == 0 && hist.at<float>(i + 1,0) > 0)) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}

float calcMHWScore(std::vector<int> scores)
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

void filter_gray_inverted_image(std::vector<segment> segments, int width, int height, Mat& gray_inverted_image) {

    std::vector<std::tuple<int,char>> ints;
    for (segment s : segments) {
       ints.push_back(std::make_tuple(s.y, 'b'));
        ints.push_back(std::make_tuple(s.y + s.height, 'e'));
    }

    sort(ints.begin(), ints.end(), SortSegments());
    std::stack<int> st;

    std::vector<std::tuple<int,int>> ends;
    int begin = 0;
    int end = 0;

    for (std::tuple<int,char> t : ints) {
        if (st.size() == 0) {
           begin = get<0>(t);
        }
        if (get<1>(t) == 'b') {
            st.push(get<0>(t));
        } else {
            st.pop();
        }
        if (st.size()==0) {
           end = get<0>(t);
           ends.push_back(std::make_tuple(begin,end));
        }
    }

    if (ends.size() > 0) {
        for (std::tuple<int,int> e : ends) {
            gray_inverted_image(cv::Range(get<0>(e), get<1>(e)), cv::Range(0, width)).setTo(0);
        }
    }

}

bool build_well_formed_page(cv::Mat& image, Mat& gray_inverted_image) {
    Mat blurred;
    GaussianBlur(image, blurred, Size(3, 3), 0);
    Canny( blurred, blurred, 10, 200, 3 );
    std::vector<vector<cv::Point> > contours;
    std::vector<cv::Vec4i> hierarchy;
    findContours( blurred, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0) );

    std::vector<std::vector<Point> > contours_poly( contours.size() );
    std::vector<float> heights( contours.size() );
    std::vector<float> filtered_heights;
    std::vector<segment> segments( contours.size() );

    for( int i = 0; i < contours.size(); i++ )
    { approxPolyDP( Mat(contours[i]), contours_poly[i], 3, true );
        auto brect = boundingRect( Mat(contours_poly[i]) );
        heights[i] = brect.height;
        segments[i] = {brect.y,brect.height};
    }

    double sum = std::accumulate(heights.begin(), heights.end(), 0.0);
    double mean = sum / heights.size();

    double sq_sum = std::inner_product(heights.begin(), heights.end(), heights.begin(), 0.0);
    double stdev = std::sqrt(sq_sum / heights.size() - mean * mean);

    auto mx = std::max_element(heights.begin(), heights.end());


    std::vector<segment> high_objects;
    std::vector<segment> normal_objects;

    for (segment s : segments) {
        if (s.height > mean + 6*stdev) {
            high_objects.push_back(s);
        }
    }

    filter_gray_inverted_image(high_objects, image.cols, image.rows, gray_inverted_image);
    return true;


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

int max_ind(std::vector<std::tuple<int,int>> zr, double threshold) {
    std::vector<std::tuple<int,int>> gaps;
    for (int i=0;i<zr.size();i++) {
        int gap = std::get<1>(zr.at(i)) - std::get<0>(zr.at(i));
        if (gap > threshold) {
            gaps.push_back(std::make_tuple(i, std::get<1>(zr.at(i)) - std::get<0>(zr.at(i))));
        }
    }
    int max = -1;
    int maxind = -1;
    if (gaps.size() > 0) {
        for (int i=0;i<gaps.size();i++) {
            int ind = std::get<0>(gaps.at(i));
            int gap = std::get<1>(gaps.at(i));

            if (gap > max){
                max = gap;
                maxind = ind;
            }
        }
        return maxind;
    } else {
        return -1;
    }
}

