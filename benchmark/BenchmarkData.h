#pragma once
#include <vector>
#include <iostream>
#include <XorShift64.h>
#include <chrono>
#include <unordered_set>
#include <algorithm>
#include <fstream>

std::string stringOfLength(size_t length, util::XorShift64 &prng) {
    std::string key(length, 'x');
    size_t i = 0;
    while (i < (length & ~0xf)) {
        *((uint64_t*) (key.data() + i)) = prng();
        i += 8;
    }
    while (i < length) {
        key[i] = (char) prng();
        i += 1;
    }
    return key;
}

std::vector<std::string> randomUniformStrings(size_t n, size_t length, size_t commonPrefixLength) {
    std::unordered_set<std::string> keys;
    keys.reserve(n);
    size_t seed = time(nullptr);
    std::cout<<"Seed: "<<seed<<std::endl;
    util::XorShift64 prng(seed);
    std::string prefix = stringOfLength(commonPrefixLength, prng);
    while (keys.size() < n) {
        std::string key = prefix + stringOfLength(length, prng);
        keys.insert(key);
    }
    std::vector<std::string> dataset;
    dataset.insert(dataset.end(), keys.begin(), keys.end());
    std::sort(dataset.begin(), dataset.end());
    return dataset;
}

std::vector<std::string> loadFile(std::string &filename, size_t maxStrings) {
    std::cout<<"Loading input file"<<std::endl;
    std::vector<std::string> inputData;
    std::ifstream stream(filename);
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
