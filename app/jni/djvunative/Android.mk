LOCAL_PATH := $(call my-dir)

CVROOT := $(LOCAL_PATH)/../../../opencv/sdk/native/jni

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
include $(CVROOT)/OpenCV.mk


LOCAL_MODULE := native-lib


LOCAL_C_INCLUDES := \
		$(LOCAL_PATH)/../djvu/djvulibre \
        $(LOCAL_PATH)/../libjpeg-version-9-android/libjpeg9 \
		$(LOCAL_PATH)/.. \
		$(LOCAL_PATH)/../lz4 \
		$(LOCAL_PATH)/../pdfium/fpdfsdk/include  \
		$(LOCAL_PATH)/../mupdf/include  \
		$(LOCAL_PATH)/../boost/include \
		$(CVROOT)/include


LOCAL_STATIC_LIBRARIES := myview djvu libjpeg9 liblz4  libopencv_imgproc libopencv_imgcodecs libopencv_core flann cpufeatures libboost_system libboost_graph c++_static


# LOCAL_CFLAGS += -DHAVE_CONFIG_H -frtti -fexceptions -fopenmp -w -Ofast -DNDEBUG -nostdlib++
LOCAL_CFLAGS += -DHAVE_CONFIG_H -frtti -fexceptions -fopenmp -w -Ofast -DNDEBUG
LOCAL_LDLIBS += -llog -lz -L$(SYSROOT)/usr/lib
LOCAL_LDFLAGS += -ldl -landroid -fopenmp  

LOCAL_C_INCLUDES += common.h pdf-lib.h ImageLoader.h PageSegmenter.h Enclosure.h


LOCAL_SRC_FILES := \
	common.cpp PageSegmenter.cpp Enclosure.cpp djvu-lib.cpp pdf-lib.cpp


include $(BUILD_SHARED_LIBRARY)


