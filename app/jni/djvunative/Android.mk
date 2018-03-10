LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := native-lib

LOCAL_CFLAGS := -DHAVE_CONFIG_H

LOCAL_C_INCLUDES := \
		/Users/sergey/code/android/djvuviewer/app/jni/djvu/djvulibre \
		/Users/sergey/code/android/djvuviewer/app/jni/libjpeg-version-9-android/libjpeg9


LOCAL_STATIC_LIBRARIES := djvu libjpeg9

LOCAL_SRC_FILES := \
	native-lib.cpp


include $(BUILD_SHARED_LIBRARY)
