#pragma once
#include <vector>
#include <iostream>
#include <XorShift64.h>
#include <chrono>
#include <unordered_set>
#include <algorithm>
#include <fstream>
#include <cstring>

std::vector<std::string> loadStringFile(std::string &filename, size_t maxStrings) {
    std::cout<<"Loading input file"<<std::endl;
    std::vector<std::string> inputData;
    std::ifstream stream(filename);
    if (!stream) throw std::system_error(errno, std::system_category(), "failed to open " + filename);
    const int MAX_LENGTH = 524288;
    char* line = new char[MAX_LENGTH];
    while (stream.getline(line, MAX_LENGTH)) {
        if (!inputData.empty()) {
            if (strcmp(inputData.back().c_str(), line) > 0) {
                throw std::runtime_error("Not sorted or duplicate key");
            }
        }
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

std::vector<uint64_t> loadInt64File(std::string &filename, size_t maxN) {
    std::ifstream fileIn(filename, std::ios::in | std::ios::binary);
    if (!fileIn) throw std::system_error(errno, std::system_category(), "failed to open " + filename);
    size_t size = 0;
    fileIn.read(reinterpret_cast<char *>(&size), sizeof(size_t));
    size = std::min(size, maxN);
    std::cout<<"Loading input file of size "<<size<<std::endl;
    std::vector<uint64_t> inputData(size);
    fileIn.read(reinterpret_cast<char *>(inputData.data()), size * sizeof(uint64_t));
    fileIn.close();
    std::cout<<"Checking if input data is sorted"<<std::endl;
    for (size_t i = 1; i < inputData.size(); i++) {
        if (inputData.at(i) < inputData.at(i - 1)) {
            throw std::runtime_error("Not sorted or duplicate key");
        }
    }
    std::cout<<"Loaded "<<inputData.size()<<" integers"<<std::endl;
    return inputData;
}

std::vector<uint64_t> loadInt32File(std::string &filename, size_t maxN) {
    std::ifstream fileIn(filename, std::ios::in | std::ios::binary);
    if (!fileIn) throw std::system_error(errno, std::system_category(), "failed to open " + filename);
    uint32_t size = 0;
    fileIn.read(reinterpret_cast<char *>(&size), sizeof(uint32_t));
    size = std::min<uint32_t>(size, maxN);
    std::cout<<"Loading input file of size "<<size<<std::endl;
    std::vector<uint32_t> inputData(size);
    fileIn.read(reinterpret_cast<char *>(inputData.data()), size * sizeof(uint32_t));
    fileIn.close();
    std::cout<<"Loaded "<<inputData.size()<<" integers"<<std::endl;
    std::cout<<"Converting to uint64"<<std::endl;
    std::vector<uint64_t> inputDataConverted;
    inputDataConverted.reserve(size);
    for (size_t i = 0; i < inputData.size(); i++) {
        inputDataConverted.push_back(inputData.at(i));
        if (i > 0 && inputData.at(i) <= inputData.at(i - 1)) {
            throw std::runtime_error("Not sorted or duplicate key");
        }
    }
    return inputDataConverted;
}
