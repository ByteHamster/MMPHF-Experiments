#include <tlx/cmdline_parser.hpp>
#include <vector>
#include <iostream>
#include "benchmark/BenchmarkData.h"

int main(int argc, char** argv) {
    size_t from = 0;
    size_t to = 0;
    std::string filename;
    std::string type = "uniform";

    tlx::CmdlineParser cmd;
    cmd.add_bytes("from", from, "Which index to start printing");
    cmd.add_bytes("to", to, "Which index to stop printing");
    cmd.add_string('f', "filename", filename, "Input filename");
    cmd.add_string('t', "type", type, "Type of data to generate. Values: uniform, gap, normal, exponential");

    if (!cmd.process(argc, argv)) {
        return 1;
    }

    std::vector<uint64_t> input;
    std::cout<<"Loading"<<std::endl;
    if (type == "int64") {
        input = loadInt64File(filename, UINT64_MAX);
    } else if (type == "int32") {
        input = loadInt32File(filename, UINT64_MAX);
    } else {
        std::cerr<<"Unknown input type"<<std::endl;
        return 1;
    }
    if (to == 0) {
        to = input.size();
    }

    size_t positionOfSlash = filename.find_last_of('/');
    std::string datasetName = positionOfSlash == std::string::npos ? filename : filename.substr(positionOfSlash + 1);

    for (size_t i = from; i < input.size() && i < to; i += (to - from) / 200) {
        std::cout<<"RESULT dataset="<<datasetName<<" x="<<i<<" y="<<input.at(i)<<std::endl;
    }

    return 0;
}
