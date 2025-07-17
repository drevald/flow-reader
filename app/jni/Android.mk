LOCAL_SHORT_COMMANDS := true
LOCAL_PATH := $(call my-dir)
include $(call all-subdir-makefiles)
LOCAL_CFLAGS    += -fopenmp -static-openmp
LOCAL_CPPFLAGS  += -fopenmp -static-openmp
LOCAL_LDLIBS    += -static-openmp
