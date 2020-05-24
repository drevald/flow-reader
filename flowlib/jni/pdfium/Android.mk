LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := myview
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/lib/libmyview.a
include $(PREBUILT_STATIC_LIBRARY)
