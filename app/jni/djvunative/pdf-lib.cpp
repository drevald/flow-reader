//
// Created by Mikhno Sergey (Galexis) on 2019-05-29.
//

#include "pdf-lib.h"

#include "common.h"


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
    put_glyphs(env, mat, list);
    free((void*)buffer);
    mat.release();

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "pdf glyphs duration%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", duration);

    return array;

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);
    int width = static_cast<int>(FPDF_GetPageWidth(page))*8;
    int height = static_cast<int>(FPDF_GetPageHeight(page))*8;

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




