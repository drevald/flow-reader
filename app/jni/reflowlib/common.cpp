#include "common.h"
#include "Xycut.h"
#include "PageSegmenter.h"
#include "Reflow.h"
#include "Enclosure.h"

Pix *mat8ToPix(cv::Mat *mat8)
{
    Pix *pixd = pixCreate(mat8->size().width, mat8->size().height, 8);
    for(int y=0; y<mat8->rows; y++) {
        for(int x=0; x<mat8->cols; x++) {
            pixSetPixel(pixd, x, y, (l_uint32) mat8->at<uchar>(y,x));
        }
    }
    return pixd;
}

cv::Mat pix8ToMat(Pix *pix8)
{
    cv::Mat mat(cv::Size(pix8->w, pix8->h), CV_8UC1);
    uint32_t *line = pix8->data;
    for (uint32_t y = 0; y < pix8->h; ++y) {
        for (uint32_t x = 0; x < pix8->w; ++x) {
            mat.at<uchar>(y, x) = GET_DATA_BYTE(line, x);
        }
        line += pix8->wpl;
    }
    return mat;
}


std::pair<std::vector<int>,std::vector<float>> make_hist(std::vector<int>& v, int num_buckets, int min, int max) {
   
    int min_elt = min;
    int max_elt = max;

    int length = max_elt - min_elt;
    double bucket_size = length / (double)num_buckets;

    std::vector<int> histogram(num_buckets);
   
    for (int i=0;i<v.size();i++) {
        int elt = v.at(i);
        int bucket = (int)ceil((float)(elt-min_elt) / bucket_size) - 1;
        if (bucket < 0) {
            bucket  = 0;
        } else if (bucket > (num_buckets - 1)) {
            bucket  = num_buckets - 1;
        }
        if (elt == min_elt) {
            histogram[0] += 1;
        }
        histogram[bucket] += 1;
       
    }

    std::vector<float> steps;
    for (int i=0;i<=num_buckets;i++) {
        float t = (min_elt + length/(float)num_buckets * (i));
        steps.push_back(t);
    }

    return std::make_pair(histogram, steps);
}

jobject splitMat(cv::Mat& mat, JNIEnv *env) {
    int w = mat.size().width;
    int h = mat.size().height;
    cv::Mat upper = mat(cv::Rect(0,0,w, h/2));
    cv::Mat lower = mat(cv::Rect(0,h/2,w, h - h/2));

    std::vector<uchar> buff_upper;//buffer for coding
    cv::imencode(".png", upper, buff_upper);

    std::vector<uchar> buff_lower;//buffer for coding
    cv::imencode(".png", lower, buff_lower);

    size_t sizeInBytesUpper = buff_upper.size();
    jbyteArray array_upper = env->NewByteArray(sizeInBytesUpper);
    env->SetByteArrayRegion(array_upper, 0, sizeInBytesUpper, (jbyte *) &buff_upper[0]);
    size_t sizeInBytesLower = buff_lower.size(); //new_image.total() * new_image.elemSize();
    jbyteArray array_lower = env->NewByteArray(sizeInBytesLower);
    env->SetByteArrayRegion(array_lower, 0, sizeInBytesLower, (jbyte *) &buff_lower[0]);


    static jclass java_util_ArrayList      = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    static jmethodID java_util_ArrayList_     = env->GetMethodID(java_util_ArrayList, "<init>", "(I)V");
    static jmethodID java_util_ArrayList_add  = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");

    jobject arrayList = env->NewObject(java_util_ArrayList, java_util_ArrayList_, 1);
    env->CallBooleanMethod(arrayList, java_util_ArrayList_add, array_upper);
    env->CallBooleanMethod(arrayList, java_util_ArrayList_add, array_lower);

    return arrayList;

}

double deviation(vector<int> v, double ave) {
    double E=0;
    double inverse = 1.0 / static_cast<double>(v.size());
    for(unsigned int i=0;i<v.size();i++)
    {
        E += pow(static_cast<double>(v[i]) - ave, 2);
    }
    return sqrt(inverse * E);
}

std::vector<glyph> preprocess(cv::Mat& image, cv::Mat& rotated_with_pictures) {
    
    cv::Mat clone = image.clone();
    
    rotated_with_pictures = image.clone();
    cv::Mat for_pictures = image.clone();
    
    int width = image.size().width;
    int height = image.size().height;
    
    // detect skew angle
    /*
    cv::Mat mat;
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(80,1));
    cv::dilate(clone, mat, kernel, cv::Point(-1, -1), 1);
    
    cv::Mat labels = cv::Mat(image.size(), image.type());
    cv::Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    cv::Mat centComponents = Mat::zeros(Size(0, 0), 0);
    connectedComponentsWithStats(mat, labels, rectComponents, centComponents);
    
    std::vector<int> line_rect_numbers;
    float angle = 0;
    float a = 0;
    for (int i = 1; i < rectComponents.rows; i++) {
        
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect r(x, y, w, h);
        
        if (w / h > 10) {
            line_rect_numbers.push_back(i);
        }
        
    }
    
    int count = 0;
    for (int i=0;i<line_rect_numbers.size(); i++) {
        cv::Mat mask_i = labels == line_rect_numbers.at(i);
        
        // Compute the contour and set it empty if too big
        vector<vector<Point>> contours;
        findContours(mask_i.clone(), contours, RETR_EXTERNAL, CHAIN_APPROX_NONE);
        
        if (!contours.empty()) {
            std::vector<cv::Point> contour = contours.at(0);
            cv::RotatedRect box = cv::minAreaRect(cv::Mat(contour));
            a = box.angle;
            if (a < -45) {
                a = (90 + a);
            }
            if (a != 0) {
                count++;
            }
            
            angle += a;
        }
        
    }
    
    std::vector<cv::Point> points;
    cv::Mat_<uchar>::iterator it = clone.begin<uchar>();
    cv::Mat_<uchar>::iterator end = clone.end<uchar>();
    for (; it != end; ++it)
        if (*it)
            points.push_back(it.pos());
    
    cv::RotatedRect box = cv::minAreaRect(cv::Mat(points));
    
    angle = angle / count;
    
    if (count > 10) {
        cv::Mat rot_mat = cv::getRotationMatrix2D(box.center, angle , 1);
        cv::warpAffine(image, image, rot_mat, image.size(), cv::INTER_CUBIC);
        cv::warpAffine(rotated_with_pictures, rotated_with_pictures, rot_mat, image.size(), cv::INTER_CUBIC);
    }
    
    
    // end detect
    
    // remove big components - defects of scanning
    //
    */

    cv::Mat labels = cv::Mat(image.size(), image.type());
    cv::Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    cv::Mat centComponents = Mat::zeros(Size(0, 0), 0);

    
    connectedComponentsWithStats(image, labels, rectComponents, centComponents);
    std::vector<cv::Rect> rects;
    
    for (int i = 1; i < rectComponents.rows; i++) {
        
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect r(x, y, w, h);
        rects.push_back(r);
        
        Rect upper(0,0,width,0.03*height);
        Rect lower(0,0.97*height,width,0.03*height);
        Rect left(0,0,0.03*width,height);
        Rect right(0.97*width,0,0.03*width,height);
        

        bool intersects_sides = ( (r & upper).area() > 0 ||
                                   (r & lower).area() > 0 ||
                                   (r & left).area() > 0 ||
                                   (r & right).area() > 0
                                   );
        
        if (intersects_sides) {
            cv::Mat mask_i = labels == i;
                 
            // Compute the contour and set it empty if too big
            vector<vector<Point>> contours;
            findContours(mask_i.clone(), contours, RETR_EXTERNAL, CHAIN_APPROX_NONE);
        
                 
             if (!contours.empty()) {
                 // if convex must intersect sides more than 50%
                 // contour area > 50 % of bounding rectangle
                 double cntArea = cv::contourArea(contours.at(0));
                 cv:Rect bRect = cv::boundingRect(contours.at(0));
                 
                 if (cntArea > 0.5*bRect.area()) {
                     // 50 percent intersection
                     bool intersects_sides_much = ( (r & upper).area() > 0.5*r.area() ||
                     (r & lower).area() > 0.5*r.area() ||
                     (r & left).area() > 0.5*r.area() ||
                     (r & right).area() > 0.5*r.area());
                     
                     if (intersects_sides_much) {
                         cv::drawContours(image, contours, -1, cv::Scalar(0), -1);
                         cv::drawContours(clone, contours, -1, cv::Scalar(0), -1);
                     }
                     
                     
                 } else {
                       
                       cv::drawContours(image, contours, -1, cv::Scalar(0), -1);
                       cv::drawContours(clone, contours, -1, cv::Scalar(0), -1);
                 }
                 
             }
        }
    
    }
    
    // end removal
    
    // detect pictures after rotation
    
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(9,2));
    cv::dilate(image, clone, kernel, cv::Point(-1, -1), 2);
    
    connectedComponentsWithStats(clone, labels, rectComponents, centComponents);
    std::vector<int> heights;
    
    rects = std::vector<cv::Rect>();
    
    for (int i = 1; i < rectComponents.rows; i++) {
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect r(x, y, w, h);
        rects.push_back(r);
        
    }
    
    
    Enclosure enc(rects);
    set<array<int, 4>> s = enc.solve();
    
    std::vector<cv::Rect> new_rects;
    
    for (auto it = s.begin(); it != s.end(); ++it) {
        array<int, 4> a = *it;
        int x = -get<0>(a);
        int y = -get<1>(a);
        int width = get<2>(a) - x;
        int height = get<3>(a) - y;
        heights.push_back(height);
        new_rects.push_back(cv::Rect(x,y,width, height));
        
    }
    
    float average_height = std::accumulate(heights.begin(), heights.end(), 0.0)/heights.size();
    
    double stddev = deviation(heights, average_height);
    
    
    for (int i=0;i<new_rects.size();i++) {
        cv::Rect r = new_rects.at(i);
        clone(r).setTo(255);
    }
    
    connectedComponentsWithStats(clone, labels, rectComponents, centComponents);
    heights = std::vector<int>();
    
    rects = std::vector<cv::Rect>();
    
    for (int i = 1; i < rectComponents.rows; i++) {
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect r(x, y, w, h);
        rects.push_back(r);
    }
    
    //rects = new_rects;
    
    std::vector<glyph> pic_glyphs;
    
    for (int i=0; i<rects.size(); i++) {
        cv::Rect r = rects.at(i);
        if (r.height > 4*average_height + 2*stddev) {
            cv::Mat m = clone(r).clone();
            threshold(m, m, 0, 255, cv::THRESH_BINARY);
            
            int count_pixels = cv::countNonZero(m);
           
            if (count_pixels > 0.95*m.size().height*m.size().width) {
                glyph g;
                g.x = r.x;
                g.y = r.y;
                g.height = r.height;
                g.width = r.width;
                g.is_last = 1;
                g.indented = 1;
                g.baseline_shift = 0;
                g.is_picture = 1;
                g.line_height = g.height;
                g.is_space = 0;
                pic_glyphs.push_back(g);
                clone(r).setTo(0);
                image(r).setTo(0);
            }

        }
    }
    
    
    // end detection
    
    
    kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(2,2));
    cv::erode(clone, clone, kernel, cv::Point(-1, -1), 1);
    
    cv::Mat hist;
    //horizontal
    reduce(clone, hist, 0, cv::REDUCE_SUM, CV_32F);
    std::vector<cv::Point> locations;
    cv::findNonZero(hist, locations);
    
    
    int left = locations.at(0).x;
    int right = locations.at(locations.size()-1).x;
    //vertical
    reduce(clone, hist, 1, cv::REDUCE_SUM, CV_32F);
    cv::findNonZero(hist, locations);
    
    int upper = locations.at(0).y;
    int lower = locations.at(locations.size()-1).y;
    
    cv::Rect rect(0, 0, width, upper);
    image(rect).setTo(0);
    
    rect = cv::Rect(0, lower, width, height-lower);
    image(rect).setTo(0);
    
    rect = cv::Rect(0, 0, left, height);
    image(rect).setTo(0);
    
    rect = cv::Rect(right, 0, width-right, height);
    image(rect).setTo(0);
    
    return pic_glyphs;
    
}
std::vector<glyph> convert_java_glyphs(JNIEnv *env, jobject list) {
    // get glyphs

    jclass listcls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    jclass pageGlyphInfoCls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/veve/flowreader/model/PageGlyphInfo")));
    jmethodID sizeMethod = env->GetMethodID(listcls, "size", "()I");

    int listsize = (int)env->CallIntMethod(list, sizeMethod);
    jmethodID getMethod = env->GetMethodID(listcls, "get", "(I)Ljava/lang/Object;");

    std::vector<glyph> glyphs;
    if (listsize > 0) {
        jmethodID getXMethod = env->GetMethodID(pageGlyphInfoCls, "getX", "()I");
        jmethodID getYMethod = env->GetMethodID(pageGlyphInfoCls, "getY", "()I");
        jmethodID getWidthMethod = env->GetMethodID(pageGlyphInfoCls, "getWidth", "()I");
        jmethodID getHeightMethod = env->GetMethodID(pageGlyphInfoCls, "getHeight", "()I");
        jmethodID getIndentedMethod = env->GetMethodID(pageGlyphInfoCls, "isIndented", "()Z");
        jmethodID getBaselineShiftMethod = env->GetMethodID(pageGlyphInfoCls, "getBaselineShift", "()I");
        jmethodID getLastMethod = env->GetMethodID(pageGlyphInfoCls, "isLast", "()Z");
        jmethodID getSpaceMethod = env->GetMethodID(pageGlyphInfoCls, "isSpace", "()Z");

        for (int i=0;i<listsize;i++) {
            jobject gobject = env->CallObjectMethod(list, getMethod, (jint)i);
            int x = (int)env->CallIntMethod(gobject, getXMethod);
            int y = (int)env->CallIntMethod(gobject, getYMethod);
            int width = (int)env->CallIntMethod(gobject, getWidthMethod);
            int height = (int)env->CallIntMethod(gobject, getHeightMethod);
            int baseline_shift = (int)env->CallIntMethod(gobject, getBaselineShiftMethod);
            bool indented = (bool)env->CallBooleanMethod(gobject, getIndentedMethod);
            bool last = (bool)env->CallBooleanMethod(gobject, getLastMethod);
            bool space = (bool)env->CallBooleanMethod(gobject, getSpaceMethod);
            glyph g;
            g.x = x;
            g.y = y;
            g.height = height;
            g.width = width;
            g.indented = indented;
            g.is_last = last;
            g.is_space = space;
            g.baseline_shift = baseline_shift;
            glyphs.push_back(g);
            env->DeleteLocalRef(gobject);
        }

    }

    return glyphs;
    // end
}




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

            __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "glyphs count = %d\n", glyphs.size());

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
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(ZIIIIIIZZ)V");

    if (constructor == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "constructor is NULL");
    }


    for (glyph g : glyphs) {

        jobject object = env->NewObject(clz, constructor, g.indented, g.x, g.y, g.width, g.height, g.line_height, g.baseline_shift, g.is_space, g.is_last);

        env->CallBooleanMethod(
                list,
                addMethod,
                object);

        env->DeleteLocalRef(object);

    }


}

void reflow(cv::Mat& cvMat, cv::Mat& new_image, float scale, int page_width,  JNIEnv* env, std::vector<glyph> savedGlyphs, jobject list, std::vector<glyph> pic_glyphs, cv::Mat rotated_with_pictures, bool preprocessing, float margin) {
    //const cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(2, 2));
    //cv::dilate(cvMat, cvMat, kernel, cv::Point(-1, -1), 1);

    std::vector<glyph> glyphs;

    if (savedGlyphs.empty()){
        glyphs = get_glyphs(cvMat);

        for (glyph g : pic_glyphs) {
            int y = g.y - g.height;
            auto it = std::find_if(glyphs.begin(), glyphs.end(), [y] (const glyph& gl) {return gl.y - gl.height > y;} );
            glyphs.insert(it, g);
        }
        put_glyphs(env, glyphs, list);
    } else {
        glyphs = savedGlyphs;
    }


    if (glyphs.size() > 0) {

        try {
            Reflow reflower(cvMat, rotated_with_pictures, glyphs);
            new_image = reflower.reflow(scale, page_width, margin);
        } catch (...) {
            __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "exception occurred");
            //cv::bitwise_not(rotated_with_pictures, rotated_with_pictures);
            new_image = rotated_with_pictures;
        }
    } else {
        //cv::bitwise_not(cvMat, cvMat);
        new_image = cvMat;
    }

}


std::vector<std::tuple<int, int>> one_runs(const cv::Mat &hist) {
    int w = hist.cols > hist.rows ? hist.cols : hist.rows;

    vector<std::tuple<int, int>> return_value;

    if (hist.cols > hist.rows) {


        int pos = 0;
        for (int i = 0; i < w; i++) {
            if ((i == 0 && hist.at<float>(0, i) > 0) ||
                (i > 0 && hist.at<float>(0, i) > 0 && hist.at<float>(0, i - 1) == 0)) {
                pos = i;
            }

            if ((i == w - 1 && hist.at<float>(0, i) > 0) ||
                (i < w - 1 && hist.at<float>(0, i) > 0 && hist.at<float>(0, i + 1) == 0)) {
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
