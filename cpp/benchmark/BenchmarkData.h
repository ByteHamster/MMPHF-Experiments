#pragma once
#include <vector>
#include <iostream>
#include <XorShift64.h>
#include <chrono>
#include <unordered_set>
#include <algorithm>
#include <fstream>

std::vector<std::string> loadStringFile(std::string &filename, size_t maxStrings) {
    std::cout<<"Loading input file"<<std::endl;
    std::vector<std::string> inputData;
    std::ifstream stream(filename);
    if (!stream) throw std::system_error(errno, std::system_category(), "failed to open " + filename);
    const int MAX_LENGTH = 524288;
    char* line = new char[MAX_LENGTH];
    while (stream.getline(line, MAX_LENGTH)) {
        inputData.emplace_back(line);
        if (inputData.size() >= maxStrings) {
            break;
        }
    }
    std::cout<<"Sorting input data"<<std::endl;
    std::sort(inputData.begin(), inputData.end());
    std::cout<<"Loaded "<<inputData.size()<<" strings"<<std::endl;
    return inputData;
}

std::vector<uint64_t> loadIntegerFile(std::string &filename, size_t maxN) {
    std::cout<<"Loading input file"<<std::endl;
    std::ifstream fileIn(filename, std::ios::in | std::ios::binary);
    if (!fileIn) throw std::system_error(errno, std::system_category(), "failed to open " + filename);
    size_t size = 0;
    fileIn.read(reinterpret_cast<char *>(&size), sizeof(size_t));
    size = std::min(size, maxN);
    std::vector<uint64_t> inputData(size);
    fileIn.read(reinterpret_cast<char *>(inputData.data()), size * sizeof(uint64_t));
    fileIn.close();
    std::cout<<"Sorting input data"<<std::endl;
    std::sort(inputData.begin(), inputData.end());
    std::cout<<"Loaded "<<inputData.size()<<" integers"<<std::endl;
    return inputData;
}
