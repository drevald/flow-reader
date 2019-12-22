//
// Created by Mikhno Sergey (Galexis) on 2019-05-29.
//

#include "pdf-lib.h"

#include "common.h"
#define RESOLUTION_MULTIPLIER  4;



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

    int height = static_cast<int>(FPDF_GetPageHeight(page))*RESOLUTION_MULTIPLIER;

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


image_format get_pdf_pixels(JNIEnv* env, jlong bookId, jint pageNumber, char** pixels) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;

    int pageno = (int)pageNumber;
    FPDF_PAGE page = FPDF_LoadPage(doc, pageno);
    int width = static_cast<int>(FPDF_GetPageWidth(page))*RESOLUTION_MULTIPLIER;
    int height = static_cast<int>(FPDF_GetPageHeight(page))*RESOLUTION_MULTIPLIER;

    int size = width * height * RESOLUTION_MULTIPLIER;

    FPDF_BITMAP bitmap = FPDFBitmap_Create(width, height, 0);
    FPDFBitmap_FillRect(bitmap, 0, 0, width, height, 0xFFFFFFFF);

    FPDF_RenderPageBitmap(bitmap, page, 0, 0, width, height, 0, 0);
    *pixels = (char*)reinterpret_cast<const char*>(FPDFBitmap_GetBuffer(bitmap));

    return image_format(width, height, size);

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeReflownBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber, jfloat scale, jboolean portrait, jfloat screen_ratio,  jobject pageSize, jobject list, jboolean preprocessing, jfloat margin) {


    // get glyphs from java

    std::vector<glyph> glyphs = convert_java_glyphs(env, list);
    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int size = format.size;
    int height = format.h;
    int width = format.w;

    Mat mat(height,width,CV_8UC4,&((char*)buffer)[0]);

    cv::cvtColor(mat, mat, cv::COLOR_BGR2GRAY);
    threshold(mat, mat, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    bool do_preprocessing = (bool)preprocessing;

    cv::Mat rotated_with_pictures;
    cv::Mat new_image;

    if (do_preprocessing) {
        std::vector<glyph> pic_glyphs = preprocess(mat, rotated_with_pictures);
        reflow(mat, new_image, scale, portrait, screen_ratio, env, glyphs, list, pic_glyphs, rotated_with_pictures, true, margin);
    } else {
        reflow(mat, new_image, scale, portrait, screen_ratio, env, glyphs, list, std::vector<glyph>(), mat, false, margin);
    }

    jclass clz = env->GetObjectClass(pageSize);

    jmethodID setPageWidthMid = env->GetMethodID(clz, "setPageWidth", "(I)V");
    jmethodID setPageHeightMid = env->GetMethodID(clz, "setPageHeight", "(I)V");
    env->CallVoidMethod(pageSize,setPageWidthMid, new_image.cols);
    env->CallVoidMethod(pageSize,setPageHeightMid, new_image.rows);

    cv::bitwise_not(new_image, new_image);

    int w = new_image.size().width;
    int h = new_image.size().height;
    cv::Mat upper = new_image(cv::Rect(0,0,w, h/2));
    cv::Mat lower = new_image(cv::Rect(0,h/2,w, h - h/2));


    std::vector<uchar> buff;//buffer for coding
    cv::imencode(".png", upper, buff);


    std::vector<uchar> buff1;//buffer for coding
    cv::imencode(".png", lower, buff1);


    //size_t sizeInBytes = new_image.total() * new_image.elemSize();
    size_t sizeInBytes = buff.size(); //new_image.total() * new_image.elemSize();

    jbyteArray array = env->NewByteArray(sizeInBytes);
    //env->SetByteArrayRegion(array, 0, sizeInBytes, (jbyte *) new_image.data);
    env->SetByteArrayRegion(array, 0, sizeInBytes, (jbyte *) &buff[0]);

    //size_t sizeInBytes = new_image.total() * new_image.elemSize();
    size_t sizeInBytes1 = buff1.size(); //new_image.total() * new_image.elemSize();

    jbyteArray array1 = env->NewByteArray(sizeInBytes1);
    //env->SetByteArrayRegion(array, 0, sizeInBytes, (jbyte *) new_image.data);
    env->SetByteArrayRegion(array1, 0, sizeInBytes1, (jbyte *) &buff1[0]);


    static jclass java_util_ArrayList      = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    static jmethodID java_util_ArrayList_     = env->GetMethodID(java_util_ArrayList, "<init>", "(I)V");
    static jmethodID java_util_ArrayList_add  = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");

    jobject arrayList = env->NewObject(java_util_ArrayList, java_util_ArrayList_, 1);
    env->CallBooleanMethod(arrayList, java_util_ArrayList_add, array);
    env->CallBooleanMethod(arrayList, java_util_ArrayList_add, array1);


    free((void*)buffer);
    mat.release();
    new_image.release();

    return arrayList;

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativePageGlyphs
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber, jobject list) {

    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int size = format.size;
    int height = format.h;
    int width = format.w;

    Mat mat(height,width,CV_8UC4,&((char*)buffer)[0]);

    cv::cvtColor(mat, mat, cv::COLOR_BGR2GRAY);
    threshold(mat, mat, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);


    std::vector<glyph> new_glyphs = get_glyphs(mat);
    put_glyphs(env, new_glyphs, list);
    size_t sizeInBytes = mat.total() * mat.elemSize();
    jbyteArray array = env->NewByteArray(sizeInBytes);
    env->SetByteArrayRegion(array, 0, sizeInBytes, (jbyte *) mat.data);
    free((void*)buffer);
    mat.release();

    return array;

}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNativeTitle
(JNIEnv *env, jclass cls, jlong bookId) {
    return get_metadata(env, bookId, "Title");
}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNativeAuthor
(JNIEnv *env, jclass cls, jlong bookId) {
    return get_metadata(env, bookId, "Author");
}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeBytes
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int size = format.size;
    int w = format.w;
    int h = format.h;

    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *) buffer);

    free((void*)buffer);
    return array;
}


JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeGrayscaleBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int size = format.size;
    int height = format.h;
    int width = format.w;

    Mat mat(height,width,CV_8UC4,&((char*)buffer)[0]);

    cv::cvtColor(mat, mat, cv::COLOR_BGR2GRAY);
    threshold(mat, mat, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    size_t sizeInBytes = mat.total() * mat.elemSize();
    jbyteArray array = env->NewByteArray(sizeInBytes);
    env->SetByteArrayRegion(array, 0, sizeInBytes, (jbyte *) mat.data);
    free((void*)buffer);
    mat.release();

    return array;

}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNumberOfPages
(JNIEnv *env, jobject obj, jlong bookId) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;
    int page_count = FPDF_GetPageCount(doc);
    return page_count;

}




