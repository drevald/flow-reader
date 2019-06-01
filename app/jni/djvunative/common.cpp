#include "common.h"

void put_glyphs(JNIEnv *env, Mat& mat, jobject& list) {
    PageSegmenter ps(mat);
    vector<glyph> glyphs = ps.get_glyphs();

    jclass listcls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    jmethodID addMethod = env->GetMethodID(listcls, "add", "(Ljava/lang/Object;)Z");
    jclass clz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/veve/flowreader/model/PageGlyphInfo")));

    if (clz == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "clz is NULL");
    }

    //jclass clz = env->FindClass("com/veve/flowreader/model/PageGlyphInfo");
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(IIIIII)V");

    if (constructor == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s\n", "constructor is NULL");
    }


    for (glyph g : glyphs) {

        jobject object = env->NewObject(clz, constructor, g.x, g.y, g.width, g.height, g.line_height, g.baseline_shift);

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

double TimeSpecToSeconds(struct timespec* ts) {
    return (double)ts->tv_sec + (double)ts->tv_nsec / 1000000000.0;
}
