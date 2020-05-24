LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := djvu
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/lib/libdjvu.a
include $(PREBUILT_STATIC_LIBRARY)
