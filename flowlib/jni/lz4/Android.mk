LOCAL_PATH:= $(call my-dir)
liblz4_src_files := lz4.c lz4hc.c lz4frame.c xxhash.c
include $(CLEAR_VARS)
LOCAL_MODULE := liblz4
LOCAL_CFLAGS := -Ofast -DNDEBUG
LOCAL_SRC_FILES := $(liblz4_src_files)
include $(BUILD_STATIC_LIBRARY)
