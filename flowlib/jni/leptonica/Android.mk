LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libleptonica

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../png \
	$(LOCAL_PATH)/../jpeg9/include/libjpeg9

LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/lib/libleptonica.a
include $(PREBUILT_STATIC_LIBRARY)
