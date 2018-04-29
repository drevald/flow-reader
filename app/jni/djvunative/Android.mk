LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := native-lib

LOCAL_CFLAGS := -DHAVE_CONFIG_H

LOCAL_C_INCLUDES := \
		$(LOCAL_PATH)/../djvu/djvulibre \
		$(LOCAL_PATH)/../libjpeg-version-9-android/libjpeg9


LOCAL_STATIC_LIBRARIES := djvu libjpeg9

LOCAL_SRC_FILES := \
	native-lib.cpp


include $(BUILD_SHARED_LIBRARY)
