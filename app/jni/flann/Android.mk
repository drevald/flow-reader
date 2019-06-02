LOCAL_PATH := $(call my-dir)

#APP_ABI := x86_64

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ \
		$(LOCAL_PATH)/../lz4


LOCAL_MODULE    := flann
LOCAL_CFLAGS    := -Ofast -frtti -fopenmp -DNDEBUG -fexceptions -DHAVE_CONFIG_H
LOCAL_LD_FLAGS  := -fopenmp


ifeq ($(TARGET_ARCH_ABI),armeabi)
    LOCAL_ARM_MODE := arm
endif # TARGET_ARCH_ABI == armeabi


LOCAL_SRC_FILES := flann_cpp.cpp

include $(BUILD_STATIC_LIBRARY)
