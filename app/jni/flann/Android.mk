LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := flann

LOCAL_C_INCLUDES := \
		$(LOCAL_PATH)/../lz4

LOCAL_STATIC_LIBRARIES := lz4
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/lib/libflann.a
include $(PREBUILT_STATIC_LIBRARY)
