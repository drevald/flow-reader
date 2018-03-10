#include <jni.h>
#include <string>
#include <stdio.h>
#include <errno.h>

#include <libdjvu/ddjvuapi.h>

#include "ImageLoader.h"


#define PIXELS 3

struct Document {
    ddjvu_context_t *ctx;
    ddjvu_document_t *doc;
};


extern "C"
JNIEXPORT jstring



JNICALL
Java_com_example_sergey_djvu_1viewer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    const char* version = ddjvu_get_version_string();
    std::string ok("OK");
    std::string err("ERROR");
    FILE *file = fopen("/storage/emulated/0/Download/matstat.djvu", "r+");

    if (file != NULL){
        fclose(file);
        return env->NewStringUTF(ok.c_str());

    } else {
        return env->NewStringUTF(strerror(errno));
    }


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

    return env->NewStringUTF("1234");
}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeWidth
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document*)bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;

    int width, height;
    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r=ddjvu_document_get_pageinfo(doc,pageno,&info))<DDJVU_JOB_OK) {

    }

    int w = info.width;
    int h = info.height;

    return (jint)info.width;

}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeHeight
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document*)bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;

    int width, height;
    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r=ddjvu_document_get_pageinfo(doc,pageno,&info))<DDJVU_JOB_OK) {

    }

    int w = info.width;
    int h = info.height;

    return (jint)info.height;

}


JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document*)bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int)pageNumber;

    ddjvu_page_t *page= ddjvu_page_create_by_pageno(doc, pageno);


    while (!ddjvu_page_decoding_done (page )) {
        ddjvu_message_wait(ctx);
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



    int width, height;
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

    ddjvu_format_t *format = ddjvu_format_create(DDJVU_FORMAT_RGB24, 0, 0);
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


    return array;




    return NULL;
}

