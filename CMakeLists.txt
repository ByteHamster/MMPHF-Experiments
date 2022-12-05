cmake_minimum_required(VERSION 3.16)
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")
project(MMPHF-Experiments)

if(CMAKE_BUILD_TYPE STREQUAL "Release" AND CMAKE_SOURCE_DIR STREQUAL CMAKE_CURRENT_SOURCE_DIR)
    add_compile_options(-march=native)
endif()

###################### Competitor setup ######################
add_library(PerfectHashingCompetitors INTERFACE)
target_compile_features(PerfectHashingCompetitors INTERFACE cxx_std_20)

add_subdirectory(extlib/directRankStoring)
target_link_libraries(PerfectHashingCompetitors INTERFACE DirectRankStoring)

add_subdirectory(extlib/pathCompressedTries EXCLUDE_FROM_ALL)
target_include_directories(PerfectHashingCompetitors SYSTEM INTERFACE extlib/pathCompressedTries)
target_link_libraries(PerfectHashingCompetitors INTERFACE succinct)

################### Benchmark build targets ###################
add_executable(Comparison comparison.cpp)
target_link_libraries(Comparison PRIVATE PerfectHashingCompetitors)
target_compile_features(Comparison PRIVATE cxx_std_20)
