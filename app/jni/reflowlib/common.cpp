#include "common.h"
#include "Xycut.h"
#include "PageSegmenter.h"




std::vector<glyph> get_glyphs(cv::Mat mat) {
    Xycut xycut(mat);
    std::vector<ImageNode> parts = xycut.xycut();
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "parts count = %d\n", parts.size());

    vector<glyph> new_glyphs;

    for (int i=0;i<parts.size(); i++) {
        ImageNode node = parts.at(i);
        Mat m = node.get_mat();
        int x = node.get_x();
        int y = node.get_y();
        cv::Size s = m.size();
        if (s.height / (float)s.width < 5) {
            cv::Rect rect(x,y,s.width, s.height);
            cv::Mat img = mat(rect);
            PageSegmenter ps(img);
            vector<glyph> glyphs = ps.get_glyphs();

            for (int j=0;j<glyphs.size(); j++) {
                glyph g = glyphs.at(j);
                if (j==0) {
                    g.indented = true;
                }
                g.x += x;
                g.y += y;
                new_glyphs.push_back(g);
            }
        }
    }
    return new_glyphs;

}

void put_glyphs(JNIEnv *env, vector<glyph>& glyphs, jobject& list) {

    jclass listcls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    jmethodID addMethod = env->GetMethodID(listcls, "add", "(Ljava/lang/Object;)Z");
    jclass clz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/veve/flowreader/model/PageGlyphInfo")));

    if (clz == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "clz is NULL");
    }

    //jclass clz = env->FindClass("com/veve/flowreader/model/PageGlyphInfo");
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(ZIIIIIIZ)V");

    if (constructor == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "constructor is NULL");
    }


    for (glyph g : glyphs) {

        jobject object = env->NewObject(clz, constructor, g.indented, g.x, g.y, g.width, g.height, g.line_height, g.baseline_shift, g.is_space);

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


std::vector<std::tuple<int, int>> one_runs(const cv::Mat &hist) {
    int w = hist.cols > hist.rows ? hist.cols : hist.rows;

    vector<std::tuple<int, int>> return_value;

    if (hist.cols > hist.rows) {


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
    } else {
        int pos = 0;
        for (int i = 0; i < w; i++) {
            if ((i == 0 && hist.at<float>(i,0) > 0) ||
                (i > 0 && hist.at<float>(i,0) > 0 && hist.at<float>(i - 1,0) == 0)) {
                pos = i;
            }

            if ((i == w - 1 && hist.at<float>(i,0) > 0) ||
                (i < w - 1 && hist.at<float>(i,0) > 0 && hist.at<float>(i + 1,0) == 0)) {
                return_value.push_back(make_tuple(pos, i));
            }
        }
        return return_value;
    }



}

std::vector<std::tuple<int,int>> zero_runs(const cv::Mat& hist) {
    int w = hist.rows > hist.cols ? hist.rows : hist.cols;

    vector<std::tuple<int, int>> return_value;
    int pos = 0;
    if (hist.rows > hist.cols) {

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
    } else {
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


}

int max_ind(std::vector<std::tuple<int,int>> zr) {
    std::vector<std::tuple<int,int>> gaps;
    for (int i=0;i<zr.size();i++) {
        gaps.push_back(std::make_tuple(i, std::get<1>(zr.at(i)) - std::get<0>(zr.at(i))));
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

int strlen16(char16_t* strarg)
{
    if(!strarg)
        return -1; //strarg is NULL pointer
    char16_t* str = strarg;
    for(;*str;++str)
        ; // empty body
    return str-strarg;
}
void remove_skew(cv::Mat& mat) {

    cv::Size size = mat.size();
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(40,1));
    cv::Mat image;
    dilate(mat, image, kernel, cv::Point(-1, -1), 1);

    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(image, contours, cv::RETR_LIST, cv::CHAIN_APPROX_NONE);

    for (int j=0; j<contours.size(); j++) {
        cv::RotatedRect rect = cv::minAreaRect(contours.at(j));
        cv::Rect r = rect.boundingRect();

        if (r.height / (double)r.width > 6 || r.width < size.width / 3) {
            std::vector<std::vector<cv::Point>> cnts;
            cnts.push_back(contours.at(j));
            cv::drawContours(image, cnts, -1, cv::Scalar(0,0,0), -1);
        }
    }

    std::vector<cv::Point> points;
    cv::Mat_<uchar>::iterator it = image.begin<uchar>();
    cv::Mat_<uchar>::iterator end = image.end<uchar>();
    for (; it != end; ++it)
        if (*it)
            points.push_back(it.pos());

    cv::RotatedRect box = cv::minAreaRect(cv::Mat(points));

    float angle = box.angle;
    if (angle < -45) {
        angle = (90 + angle);

    }

    cv::Mat rot_mat = cv::getRotationMatrix2D(box.center, angle , 1);
    cv::warpAffine(mat, mat, rot_mat, mat.size(), cv::INTER_CUBIC);

}