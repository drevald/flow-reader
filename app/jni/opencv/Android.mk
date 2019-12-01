LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libm
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libm.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := cpufeatures
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libcpufeatures.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := tbb
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libtbb.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ittnotify
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libittnotify.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_core
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libopencv_core.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_imgproc
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libopencv_imgproc.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_imgcodecs
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libopencv_imgcodecs.a
include $(PREBUILT_STATIC_LIBRARY)

ifeq ($(TARGET_ARCH_ABI),x86_64)
include $(CLEAR_VARS)
LOCAL_MODULE := ippiw 
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libippiw.a
include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),x86)
include $(CLEAR_VARS)
LOCAL_MODULE := ippiw 
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libippiw.a
include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),x86_64)
include $(CLEAR_VARS)
LOCAL_MODULE := ippicv
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libippicv.a
include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),x86)
include $(CLEAR_VARS)
LOCAL_MODULE := ippicv 
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libippicv.a
include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_MODULE := tegra_hal
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libtegra_hal.a
include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
include $(CLEAR_VARS)
LOCAL_MODULE := tegra_hal 
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libtegra_hal.a
include $(PREBUILT_STATIC_LIBRARY)
endif