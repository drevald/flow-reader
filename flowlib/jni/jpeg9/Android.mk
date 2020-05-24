LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := jpeg9
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/lib/libjpeg9.a
include $(PREBUILT_STATIC_LIBRARY)
