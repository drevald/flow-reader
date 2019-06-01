#ifndef FLOW_READER_PDF_LIB_H
#define FLOW_READER_PDF_LIB_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getBytes
        (JNIEnv *, jclass, jlong, jint);

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getPageGlyphs
        (JNIEnv *, jclass, jlong, jint, jobject);

JNIEXPORT jlong JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_openBook
        (JNIEnv *, jobject, jstring);

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBook_getNumberOfPages
        (JNIEnv *, jobject, jlong);

/*
* Class:     com_veve_flowreader_model_impl_djvu_DjvuBookPage
* Method:    getNativeWidth
* Signature: (JI)I
*/
JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeWidth
        (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_veve_flowreader_model_impl_djvu_DjvuBookPage
 * Method:    getNativeHeight
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_pdf_PdfBookPage_getNativeHeight
        (JNIEnv *, jclass, jlong, jint);

#ifdef __cplusplus
}


#endif
#endif //FLOW_READER_PDF_LIB_H
