#include <tlx/cmdline_parser.hpp>
#include <fstream>
#include <vector>
#include <iostream>
#include <ips2ra.hpp>
#include <random>

int main(int argc, char** argv) {
    size_t N = 1000000;
    std::string filename;
    std::string type = "uniform";

    tlx::CmdlineParser cmd;
    cmd.add_bytes('n', "N", N, "Number of objects to generate");
    cmd.add_string('f', "filename", filename, "Output filename");
    cmd.add_string('t', "type", type, "Type of data to generate. Values: uniform, gap");

    if (!cmd.process(argc, argv)) {
        return 1;
    }

    std::ofstream stream(filename);
    if (!stream) throw std::system_error(errno, std::system_category(), "failed to open " + filename);

    std::vector<uint64_t> data;
    data.reserve(N);

    std::cout<<"Generating"<<std::endl;
    if (type == "uniform") {
        std::mt19937_64 rng(42);
        std::uniform_int_distribution<uint64_t> dist(0, INT64_MAX);
        for (size_t i = 0; i < N; i++) {
            data.push_back(dist(rng));
        }
    } else if (type == "gap") {
        size_t NToRemove = N / 10;
        size_t spacing = INT64_MAX / (N + NToRemove);
        for (size_t i = 0; i < N + NToRemove; i++) {
            data.push_back(i * spacing);
        }
        std::mt19937_64 rng(42);
        for (size_t i = 0; i < NToRemove; i++) {
            std::uniform_int_distribution<uint64_t> dist(0, N - i);
            size_t indexToRemove = dist(rng);
            data[indexToRemove] = data.back();
            data.pop_back();
        }
    } else {
        cmd.print_usage();
        return 1;
    }

    std::cout<<"Sorting"<<std::endl;
    ips2ra::sort(data.begin(), data.end());

    std::cout<<"Checking"<<std::endl;
    if (data.size() != N) {
        std::cerr<<"Not N"<<std::endl;
        return 1;
    }
    for (size_t i = 1; i < N; i++) {
        if (data[i - 1] >= data[i]) {
            std::cerr<<"Not unique"<<std::endl;
            return 1;
        }
    }

    std::cout<<"Writing"<<std::endl;
    stream.write((char*) &N, sizeof(size_t));
    stream.write((char*) data.data(), N * sizeof(uint64_t));
    stream.close();

    std::cout<<"OK"<<std::endl;
    return 0;
}