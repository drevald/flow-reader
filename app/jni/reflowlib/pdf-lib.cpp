//
// Created by Mikhno Sergey (Galexis) on 2019-05-29.
//

#include "pdf-lib.h"
#include <sys/time.h>
#include "common.h"
#include "segmentation.h"
#define RESOLUTION_MULTIPLIER  4.1;



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

    FPDF_RenderPageBitmap(bitmap, page, 0, 0, width, height, 0, FPDF_GRAYSCALE);
    *pixels = (char*)reinterpret_cast<const char*>(FPDFBitmap_GetBuffer(bitmap));

    return image_format(width, height, size, 300);

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeReflowedBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber, jfloat scale, jint pageWidth, jobject pageSize, jobject list, jboolean preprocessing, jfloat margin) {


    // get glyphs from java

    std::vector<glyph> glyphs = convert_java_glyphs(env, list);
    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int size = format.size;
    int height = format.h;
    int width = format.w;

    Mat mat(height,width,CV_8UC4,&((char*)buffer)[0]);

    cv::cvtColor(mat, mat, cv::COLOR_RGB2GRAY);

    bool do_preprocessing = (bool)preprocessing;

    cv::Mat rotated_with_pictures;
    cv::Mat new_image;

    if (do_preprocessing) {
        std::vector<uchar> buff;//buffer for coding
        cv::imencode(".png", mat, buff);
        PIX* pix = pixReadMemPng((l_uint8*)&buff[0], buff.size()) ;
        PIX* result;
        dewarpSinglePage(pix, 127, 1, 1, 1, &result, NULL, 1);
        PIX* r = pixDeskew(result, 0);
        pixDestroy(&result);
        pixDestroy(&pix);

        cv::Mat m = pix8ToMat(r);
        threshold(m, m, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);
        cv::Mat rotated_with_pictures;
        std::vector<glyph> pic_glyphs = preprocess(m, rotated_with_pictures);
        reflow(m, new_image, scale, pageWidth, env, glyphs, list, pic_glyphs, rotated_with_pictures, true, margin);
        pixDestroy(&r);
    } else {
            cv::Mat m = mat.clone();
            //threshold(m, m, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);
            std::vector<cv::Rect> new_rects;
            std::vector<int> pic_indexes;
            std::vector<cv::Rect> rects_with_joined_captions;
            new_rects = find_enclosing_rects(m);
            if (new_rects.size() > 10) {
                auto belongs = detect_captions(m, new_rects);
                pic_indexes = join_with_captions(belongs, new_rects, rects_with_joined_captions);

                std::vector<cv::Rect> filtered_rects;
                std::vector<cv::Rect> pictures;
                std::vector<glyph> new_glyphs;

                for (int i=0;i<rects_with_joined_captions.size(); i++) {
                    cv::Rect r = rects_with_joined_captions[i];
                    if (std::find(pic_indexes.begin(), pic_indexes.end(), i) == pic_indexes.end()) {
                        glyph ng = {false, r.x, r.y, r.width, r.height, 0, 0, 0, 0, 0};
                        filtered_rects.push_back(rects_with_joined_captions[i]);
                        new_glyphs.push_back(ng);
                    } else {
                        glyph ng = {false, r.x, r.y, r.width, r.height, 0, 0, 0, 0, 1};
                        pictures.push_back(rects_with_joined_captions[i]);
                        new_glyphs.push_back(ng);
                    }
                }
                put_glyphs(env, new_glyphs, list);
                double factor = 1.0;
                new_image = find_reflowed_image(filtered_rects, pictures, factor, scale, m);
            } else {
                new_image = mat;
            }

        }

    jclass clz = env->GetObjectClass(pageSize);

    jmethodID setPageWidthMid = env->GetMethodID(clz, "setPageWidth", "(I)V");
    jmethodID setPageHeightMid = env->GetMethodID(clz, "setPageHeight", "(I)V");
    env->CallVoidMethod(pageSize,setPageWidthMid, new_image.cols);
    env->CallVoidMethod(pageSize,setPageHeightMid, new_image.rows);

    cv::bitwise_not(new_image, new_image);
    jobject arrayList = splitMat(new_image, env);

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


    std::vector<glyph> new_glyphs = get_glyphs(mat, std::vector<glyph>());
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

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_close
        (JNIEnv *env, jclass cls, jlong bookId) {
    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)bookId;
    FPDF_CloseDocument(doc);
    return 0;
}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNativeAuthor
(JNIEnv *env, jclass cls, jlong bookId) {
    return get_metadata(env, bookId, "Author");
}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeBytes
(JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    char* buffer;

    image_format format = get_pdf_pixels(env, bookId, pageNumber, &buffer);
    int w = format.w;
    int h = format.h;

    Mat mat(h,w,CV_8UC4,&((char*)buffer)[0]);
    std::vector<uchar> buff;//buffer for coding
    cv::imencode(".png", mat, buff);
    int size = buff.size();


    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *) &buff[0]);

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



