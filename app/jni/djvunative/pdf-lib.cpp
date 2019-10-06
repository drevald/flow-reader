//
// Created by Mikhno Sergey (Galexis) on 2019-05-29.
//

#include "pdf-lib.h"

#include "common.h"
#include "PageSegmenter.h"
#include "Xycut.h"

jstring get_metadata(JNIEnv *env, jlong bookId, const char* property) {
    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;
    char16_t buf[128];
    int size = FPDF_GetMetaText(doc, property, buf, 256);

    int length = strlen16(buf);

    if (size > 0 && size < 256) {
        jchar* jc = (jchar*)buf;
        return env->NewString(jc, length);
    } else {
        return NULL;
    }
}


JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeWidth
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);

    int width = static_cast<int>(FPDF_GetPageWidth(page))*4;

    return (jint)width;


}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeHeight
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);

    int height = static_cast<int>(FPDF_GetPageHeight(page))*4;

    return (jint)height;

}





JNIEXPORT jlong JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_openBook
(JNIEnv* env, jobject obj, jstring path) {


    FPDF_InitLibrary(NULL);
    const char *nativePath = env->GetStringUTFChars(path, 0);
    FPDF_DOCUMENT doc = FPDF_LoadDocument(nativePath, NULL);
    if (!doc) {
        return 1;
    }


    //FPDF_CloseDocument(doc);

    return (jlong)doc;

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativePageGlyphs
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber, jobject list) {

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;

    clock_gettime(CLOCK_MONOTONIC, &start);


    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);
    int width = static_cast<int>(FPDF_GetPageWidth(page))*4;
    int height = static_cast<int>(FPDF_GetPageHeight(page))*4;

    int size = width * height * 4;

    FPDF_BITMAP bitmap = FPDFBitmap_Create(width, height, 0);
    FPDFBitmap_FillRect(bitmap, 0, 0, width, height, 0xFFFFFFFF);

    FPDF_RenderPageBitmap(bitmap, page, 0, 0, width, height, 0, 0);
    const char* buffer =
        reinterpret_cast<const char*>(FPDFBitmap_GetBuffer(bitmap));

    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte*)buffer);

    Mat mat(height,width,CV_8UC4,&((char*)buffer)[0]);

    Xycut xycut(mat);
    std::vector<ImageNode> parts = xycut.xycut();
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
                if (j==0){
                    g.indented = true;
                }
                g.x += x;
                g.y += y;
                new_glyphs.push_back(g);
            }
        }
    }

    put_glyphs(env, new_glyphs, list);
    free((void*)buffer);
    mat.release();

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "pdf glyphs duration%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", duration);

    return array;

}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeTitle
(JNIEnv *env, jclass cls, jlong bookId) {
    return get_metadata(env, bookId, "Title");
}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeAuthor
(JNIEnv *env, jclass cls, jlong bookId) {
    return get_metadata(env, bookId, "Author");
}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeBytes
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);
    int width = static_cast<int>(FPDF_GetPageWidth(page))*4;
    int height = static_cast<int>(FPDF_GetPageHeight(page))*4;

    int size = width * height * 4;

    FPDF_BITMAP bitmap = FPDFBitmap_Create(width, height, 0);
    FPDFBitmap_FillRect(bitmap, 0, 0, width, height, 0xFFFFFFFF);

    FPDF_RenderPageBitmap(bitmap, page, 0, 0, width, height, 0, 0);
    const char* buffer =
        reinterpret_cast<const char*>(FPDFBitmap_GetBuffer(bitmap));

    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte*)buffer);

    free((void*)buffer);
    return array;
}


JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNumberOfPages
(JNIEnv *env, jobject obj, jlong bookId) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;
    int page_count = FPDF_GetPageCount(doc);
    return page_count;

}




