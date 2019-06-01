
#include "djvu-lib.h"
#include "common.h"


#define PIXELS 3


struct Document {
    ddjvu_context_t *ctx;
    ddjvu_document_t *doc;
};


JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_getNumberOfPages
        (JNIEnv *env, jobject obj, jlong bookId) {

    Document *document = (Document *) bookId;
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
        (JNIEnv *env, jobject obj, jstring path) {

    Document *d = (Document *) malloc(sizeof(struct Document));
    const char *nativePath = env->GetStringUTFChars(path, 0);
    ddjvu_context_t *ctx = ddjvu_context_create("djvu");
    ddjvu_document_t *doc = ddjvu_document_create_by_filename(ctx, nativePath, TRUE);
    d->ctx = ctx;
    d->doc = doc;
    return (jlong) d;

}

JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_openStringBook
        (JNIEnv *env, jobject obj, jstring str) {

    return env->NewStringUTF("test");
}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeWidth
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document *) bookId;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int) pageNumber;
    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r = ddjvu_document_get_pageinfo(doc, pageno, &info)) < DDJVU_JOB_OK) {

    }
    return (jint) info.width;

}

JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeHeight
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    Document *document = (Document *) bookId;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int) pageNumber;

    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r = ddjvu_document_get_pageinfo(doc, pageno, &info)) < DDJVU_JOB_OK) {

    }
    return (jint) info.height;

}

void ThrowError(JNIEnv *env, const char *msg) {
    jclass exceptionClass = env->FindClass("java/lang/RuntimeException");
    if (!exceptionClass)
        return;
    if (!msg)
        env->ThrowNew(exceptionClass, "Djvu decoding error!");
    else env->ThrowNew(exceptionClass, msg);
}

void ThrowDjvuError(JNIEnv *env, const ddjvu_message_t *msg) {
    if (!msg || !msg->m_error.message)
        ThrowError(env, "Djvu decoding error!");
    else ThrowError(env, msg->m_error.message);
}

void handleMessages(JNIEnv *env, ddjvu_context_t *ctx) {
    const ddjvu_message_t *msg;
    while ((msg = ddjvu_message_peek(ctx))) {
        switch (msg->m_any.tag) {
            case DDJVU_ERROR:
                ThrowDjvuError(env, msg);
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


void waitAndHandleMessages(JNIEnv *env, ddjvu_context_t *contextHandle) {
    ddjvu_context_t *ctx = contextHandle;
    // Wait for first message
    ddjvu_message_wait(ctx);
    // Process available messages
    handleMessages(env, ctx);
}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativePageGlyphs
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber, jobject list) {

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;

    clock_gettime(CLOCK_MONOTONIC, &start);

    Document *document = (Document *) bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int) pageNumber;

    ddjvu_page_t *page = ddjvu_page_create_by_pageno(doc, pageno);


    ddjvu_status_t r;
    ddjvu_pageinfo_t info;
    while ((r = ddjvu_document_get_pageinfo(doc, pageno, &info)) < DDJVU_JOB_OK) {

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

    unsigned int masks[] = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
    ddjvu_format_t *pixelFormat = ddjvu_format_create(DDJVU_FORMAT_RGBMASK32, 4, masks);

    ddjvu_format_set_row_order(pixelFormat, 1);
    ddjvu_format_set_y_direction(pixelFormat, 1);

    int size = w * h * 4;
    char *pixels = (char *) malloc(size);

    while (!ddjvu_page_decoding_done(page)) {
        waitAndHandleMessages(env, ctx);
    }

    jboolean ret = ddjvu_page_render(page, DDJVU_RENDER_COLOR,
                                     &prect,
                                     &rrect,
                                     pixelFormat,
                                     w * 4,
                                     (char *) pixels);


    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *) pixels);

    Mat mat(h, w, CV_8UC4, &((char *) pixels)[0]);

    put_glyphs(env, mat, list);

    mat.release();

    ddjvu_format_release(pixelFormat);

    free(pixels);

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "total duration%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", duration);

    return array;

}

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeBytes
        (JNIEnv *env, jclass cls, jlong bookId, jint pageNumber) {

    struct timespec start;
    struct timespec end;
    double elapsedSeconds;

    clock_gettime(CLOCK_MONOTONIC, &start);

    Document *document = (Document *) bookId;
    ddjvu_context_t *ctx = document->ctx;
    ddjvu_document_t *doc = document->doc;

    int pageno = (int) pageNumber;

    ddjvu_page_t *page = ddjvu_page_create_by_pageno(doc, pageno);

    while (!ddjvu_page_decoding_done (page)) {
        //ddjvu_message_wait(ctx);
        // Process available messages
        const ddjvu_message_t *msg;
        while ((msg = ddjvu_message_peek(ctx))) {
            switch (msg->m_any.tag) {
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
    while ((r = ddjvu_document_get_pageinfo(doc, pageno, &info)) < DDJVU_JOB_OK) {

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

    // ddjvu_format_t *format = ddjvu_format_create(DDJVU_FORMAT_RGB24, 0, NULL);
    unsigned int masks[] = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
    ddjvu_format_t *format = ddjvu_format_create(DDJVU_FORMAT_RGBMASK32, 4, masks);
    ddjvu_format_set_row_order(format, 1);
    ddjvu_format_set_y_direction(format, 1);

    int size = w * h * 4;
    char *pixels = (char *) malloc(size);

    int s = ddjvu_page_render(page, DDJVU_RENDER_COLOR,
                              &prect,
                              &rrect,
                              format,
                              w * 4,
                              pixels);


    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *) pixels);
    free(pixels);
    ddjvu_format_release(format);
    //free(page);

    clock_gettime(CLOCK_MONOTONIC, &end);
    elapsedSeconds = TimeSpecToSeconds(&end) - TimeSpecToSeconds(&start);
    char duration[30];
    sprintf(duration, "%f", elapsedSeconds);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", duration);

    return array;
}


