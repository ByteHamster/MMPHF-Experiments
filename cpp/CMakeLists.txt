cmake_minimum_required(VERSION 3.16)
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")
project(MMPHF-Experiments)

if(CMAKE_BUILD_TYPE STREQUAL "Release" AND CMAKE_SOURCE_DIR STREQUAL CMAKE_CURRENT_SOURCE_DIR)
    add_compile_options(-march=native)
endif()
add_compile_options(-frecord-gcc-switches)

###################### Competitor setup ######################
add_library(PerfectHashingCompetitors INTERFACE)
target_compile_features(PerfectHashingCompetitors INTERFACE cxx_std_20)

add_subdirectory(extlib/LeMonHash)
target_link_libraries(PerfectHashingCompetitors INTERFACE LeMonHash)

# Doing it "properly" with add_subdirectory fails because both ds2i and path_decomposed_tries add the same submodule.
option(SUCCINCT_USE_INTRINSICS "Use a set of intrinsics available on all x86-64 architectures" ON)
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/extlib/pathDecomposedTries/succinct/succinct_config.hpp.in
        ${CMAKE_CURRENT_SOURCE_DIR}/extlib/pathDecomposedTries/succinct/succinct_config.hpp)
add_library(pathDecomposedTries STATIC
        extlib/pathDecomposedTries/succinct/bp_vector.cpp
        extlib/pathDecomposedTries/succinct/rs_bit_vector.cpp)
target_include_directories(pathDecomposedTries SYSTEM INTERFACE extlib/pathDecomposedTries)
target_link_libraries(pathDecomposedTries INTERFACE ds2i)
target_link_libraries(PerfectHashingCompetitors INTERFACE pathDecomposedTries)

################### Benchmark build targets ###################
add_executable(Comparison comparison.cpp)
target_link_libraries(Comparison PRIVATE PerfectHashingCompetitors)
target_compile_features(Comparison PRIVATE cxx_std_20)

add_executable(GenerateData generateData.cpp)
target_link_libraries(GenerateData PRIVATE tlx ips2ra)
target_compile_features(GenerateData PRIVATE cxx_std_20)

add_executable(PlotData plotData.cpp)
target_link_libraries(PlotData PRIVATE tlx ByteHamsterUtil)
target_compile_features(PlotData PRIVATE cxx_std_20)
