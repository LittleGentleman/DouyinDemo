
cmake_minimum_required(VERSION 3.4.1)


add_library(
            FaceTracker


             SHARED

             src/main/cpp/native-lib.cpp
            src/main/cpp/FaceTrack.cpp)

#导入头文件
include_directories(src/main/cpp/include)

#指定第三方so库查找路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")

target_link_libraries(
                        FaceTracker
                        opencv_java4
                        SeetaFaceLandmarker
                        log )