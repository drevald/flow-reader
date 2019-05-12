#include <jni.h>
#include <string>
#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <math.h>

#include <libdjvu/ddjvuapi.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>
#include <flann/flann.hpp>
#include "flann/util/matrix.h"
#include "ImageLoader.h"

#define APPNAME "DJVU1"

#define PIXELS 3

using namespace cv;

using namespace flann;

struct Document {
    ddjvu_context_t *ctx;
    ddjvu_document_t *doc;
};

static double TimeSpecToSeconds(struct timespec* ts)
{
    return (double)ts->tv_sec + (double)ts->tv_nsec / 1000000000.0;
}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_getNumberOfPages
        (JNIEnv *env, jobject obj, jlong bookId) {

    Document *document = (Document*)bookId;
    ddjvu_document_t *doc = document->doc;
    ddjvu_context_t *ctx = document->ctx;

    ddjvu_message_wait(ctx);
    ddjvu_message_t *msg;
    while (msg = ddjvu_message_peek(ctx)) {
        ddjvu_message_pop(ctx);
    }


    return ddjvu_document_get_pagenum(doc);

}

JNIEXPORT jlong JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_openBook
        (JNIEnv* env, jobject obj, jstring path) {

    Document* d = (Document*)malloc(sizeof(struct Document));
    const char *nativePath = env->GetStringUTFChars(path, 0);
    ddjvu_context_t *ctx = ddjvu_context_create("djvu");
    ddjvu_document_t *doc = ddjvu_document_create_by_filename(ctx, nativePath, TRUE);
    d->ctx = ctx;
    d->doc = doc;
    return (jlong)d;

}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_openStringBook
        (JNIEnv *env, jobject obj,  jstring str) {

    return env->NewStringUTF("test");
}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeWidth
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document*)bookId;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;
    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r=ddjvu_document_get_pageinfo(doc,pageno,&info))<DDJVU_JOB_OK) {

    }
    return (jint)info.width;

}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeHeight
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document*)bookId;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;

    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r=ddjvu_document_get_pageinfo(doc,pageno,&info))<DDJVU_JOB_OK) {

    }
    return (jint)info.height;

}


JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;

    clock_gettime(CLOCK_MONOTONIC, &start);

    Document *document = (Document*)bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;

    ddjvu_page_t *page= ddjvu_page_create_by_pageno(doc, pageno);

    while (!ddjvu_page_decoding_done (page )) {
        //ddjvu_message_wait(ctx);
        // Process available messages
      const ddjvu_message_t *msg;
        while((msg = ddjvu_message_peek(ctx)))
        {
            switch (msg->m_any.tag)
            {
                case DDJVU_ERROR:

                    break;
                case DDJVU_INFO:
                    break;
                case DDJVU_DOCINFO:
                    break;
                default:
                    break;
            }
            ddjvu_message_pop(ctx);
        }
    }

    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r=ddjvu_document_get_pageinfo(doc,pageno,&info))<DDJVU_JOB_OK) {

    }

    int w = info.width;
    int h = info.height;

    ddjvu_rect_t rrect;
    ddjvu_rect_t prect;

    prect.x = 0;
    prect.y = 0;
    prect.w = w;
    prect.h = h;
    rrect = prect;

    ddjvu_format_t *format = ddjvu_format_create(DDJVU_FORMAT_BGR24, 0, NULL);
    //static uint masks[4] = { 0xff0000, 0xff00, 0xff, 0xff000000 };
    //ddjvu_format_t * format = ddjvu_format_create ( DDJVU_FORMAT_RGBMASK32, 4, masks );
    ddjvu_format_set_row_order(format, 1);
    ddjvu_format_set_y_direction(format, 1);

    int size = w * h * PIXELS;
    char *pixels = (char*)malloc(size);

    int s = ddjvu_page_render (page, DDJVU_RENDER_COLOR,
                               &prect,
                               &rrect,
                               format,
                               w*PIXELS,
                               pixels);



    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte*)pixels);
    free(pixels);
    //free(page);


    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", duration);

    return array;
}

vector<line_limit> PageSegmenter::get_line_limits() {

    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image,image);
    threshold(image,image,0,255, THRESH_OTSU | THRESH_BINARY);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    erode(image, image, kernel, Point(-1,-1), 2);
    dilate(image, image, kernel, Point(-1,-1), 2);
    vector<line_limit> v;
    return v;
}

vector<cc_result> PageSegmenter::get_cc_results(Mat &image) {
    map<tuple<double,double>,Rect> rd;
    Mat labeled(image.size(), image.type());
    Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    Mat centComponents = Mat::zeros(Size(0, 0), 0);
    connectedComponentsWithStats(image, labeled, rectComponents, centComponents);
    vector<cc_result> v;
    return v;

}


vector<glyph> PageSegmenter::get_glyphs() {

    // preprocess for the first step
    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image,image);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    dilate(image, image, kernel, Point(-1,-1), 2);




    // just a try
    float data[10][2];

    for (int i=0; i<10;i++) {
        data[i][0] = i;
        data[i][1] = i;
    }

    Matrix<float> dataset(&data[0][0], 10, 2);

    int ind[10][2];

    Matrix<int> indices(&ind[0][0], 10, 2);

    float d[10][2];

    Matrix<float> dists(&d[0][0], 10, 2);


    Index<L2<float>> index(dataset, KDTreeIndexParams(1));
    index.buildIndex();

    float q[1][2];
    q[0][0] = 5.2;
    q[0][1] = 5.3;

    Matrix<float> query(&q[0][0], 1, 2);
    // do a knn search, using 128 checks
    index.knnSearch(query, indices, dists, 30, flann::SearchParams());

    vector<glyph> v;
    return v;
}



