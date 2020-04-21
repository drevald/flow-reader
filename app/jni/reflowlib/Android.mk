
LOCAL_PATH := $(call my-dir)

CVROOT := $(LOCAL_PATH)/../../../opencv/sdk/native/jni

include $(CLEAR_VARS)

#OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=STATIC
#include $(CVROOT)/OpenCV.mk

LOCAL_MODULE := native-lib


LOCAL_C_INCLUDES := \
		$(LOCAL_PATH)/../djvu/include \
		$(LOCAL_PATH)/../opencv/include \
		$(LOCAL_PATH)/../flann/include \
		$(LOCAL_PATH)/../pdfium/include/fpdfsdk/include \
		$(LOCAL_PATH)/../jpeg9/include/libjpeg9 \
		$(LOCAL_PATH)/../lz4 \
		$(LOCAL_PATH)/../leptonica \
		$(LOCAL_PATH)/.. \
		$(LOCAL_PATH)/../boost/include \
		$(CVROOT)/include


ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
	LOCAL_STATIC_LIBRARIES := myview djvu jpeg9 lz4 libopencv_photo libopencv_imgproc libopencv_imgcodecs libopencv_core libleptonica libtiff libpng libjpeg-turbo libjasper libwebp libIlmImf flann cpufeatures ittnotify tbb tegra_hal libboost_system libboost_graph libm c++_static
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
	LOCAL_STATIC_LIBRARIES := myview djvu lz4 libopencv_photo libopencv_imgproc libopencv_imgcodecs libopencv_core libleptonica libtiff libpng libjpeg-turbo libjasper libwebp libIlmImf flann cpufeatures ittnotify tbb tegra_hal libboost_system libboost_graph libm c++_static
endif

ifeq ($(TARGET_ARCH_ABI),x86_64)
	LOCAL_STATIC_LIBRARIES := myview djvu lz4 libopencv_photo libopencv_imgproc libopencv_imgcodecs libopencv_core libleptonica libtiff libpng libjpeg-turbo libjasper libwebp libIlmImf flann cpufeatures ittnotify tbb libboost_system ippiw ippicv libboost_graph c++_static
endif

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_STATIC_LIBRARIES := myview djvu lz4 libopencv_photo libopencv_imgproc libopencv_imgcodecs libopencv_core libleptonica libtiff libpng libjpeg-turbo libjasper libwebp libIlmImf libprotobuf flann cpufeatures ittnotify tbb libboost_system ippiw ippicv libboost_graph c++_static
endif

LOCAL_ALLOW_UNDEFINED_SYMBOLS=true

LOCAL_CFLAGS += -DHAVE_CONFIG_H -frtti -fexceptions -fopenmp -w -Ofast -DNDEBUG
LOCAL_LDLIBS += -llog -lz -lm -L$(SYSROOT)/usr/lib
LOCAL_LDFLAGS += -ldl -landroid -fopenmp
LOCAL_LDFLAGS += -ldl -landroid -static-openmp

LOCAL_C_INCLUDES += common.h mylib.h LineSpacing.h Reflow.cpp PageSegmenter.h Enclosure.h pdf-lib.h  ImageNode.h Xycut.h

ifeq ($(TARGET_ARCH_ABI),x86)
LOCAL_SRC_FILES := \
	common.cpp LineSpacing.cpp Reflow.cpp  PageSegmenter.cpp Enclosure.cpp djvu-lib.cpp pdf-lib.cpp ImageNode.cpp Xycut.cpp
endif

ifeq ($(TARGET_ARCH_ABI),x86_64)
LOCAL_SRC_FILES := \
	common.cpp LineSpacing.cpp Reflow.cpp  PageSegmenter.cpp Enclosure.cpp djvu-lib.cpp pdf-lib.cpp ImageNode.cpp Xycut.cpp
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_SRC_FILES := \
	common.cpp LineSpacing.cpp Reflow.cpp  PageSegmenter.cpp Enclosure.cpp djvu-lib.cpp pdf-lib.cpp ImageNode.cpp Xycut.cpp
endif
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_SRC_FILES := \
	common.cpp LineSpacing.cpp Reflow.cpp  PageSegmenter.cpp Enclosure.cpp djvu-lib.cpp pdf-lib.cpp ImageNode.cpp Xycut.cpp
endif



include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
