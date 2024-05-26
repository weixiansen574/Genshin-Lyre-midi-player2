cmake_minimum_required(VERSION 3.4.1)

project("demo")

add_library(sendevent-jni SHARED
        sendevent-jni.c)

target_link_libraries(sendevent-jni
        android
        log)