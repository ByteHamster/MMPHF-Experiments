#include <tlx/cmdline_parser.hpp>
#include "benchmark/RecursiveDirectRankStoringContender.h"
#include "benchmark/CentroidHollowTrieContender.h"
#include "benchmark/HollowTrieContender.h"
#include "benchmark/PathDecomposedTrieContender.h"
#include "benchmark/DirectRankStoringContender.h"

int main(int argc, char** argv) {
    size_t maxN = std::numeric_limits<size_t>::max();
    std::string filename;
    std::string type = "strings";
    size_t numQueries = 100;

    tlx::CmdlineParser cmd;
    cmd.add_bytes('n', "maxN", maxN, "Truncate input file to only this number of strings if it is longer");
    cmd.add_bytes('q', "numQueries", numQueries, "Number of queries to perform");
    cmd.add_string('f', "filename", filename, "Input data set to load. List of strings, separated by newlines OR binary file of integers, if type==integers");
    cmd.add_string('t', "type", type, "Type of data to read from the file. Values: strings, int64, int32");

    if (!cmd.process(argc, argv)) {
        return 1;
    }
    size_t positionOfSlash = filename.find_last_of('/');
    Contender<std::string>::dataset = positionOfSlash == std::string::npos ? filename : filename.substr(positionOfSlash + 1);
    Contender<std::string>::numQueries = numQueries;
    Contender<uint64_t>::dataset = positionOfSlash == std::string::npos ? filename : filename.substr(positionOfSlash + 1);
    Contender<uint64_t>::numQueries = numQueries;

    if (type == "strings") {
        std::vector<std::string> input = loadStringFile(filename, maxN);
        if (input.size() < 2) {
            std::cerr<<"Input file does not contain strings"<<std::endl;
            return 1;
        }

        recursiveDirectRankStoringContenderRunner(input);
        centroidHollowTrieContenderRunner(input);
        hollowTrieContenderRunner(input);
        pathDecomposedTrieContenderRunner(input);
    } else {
        std::vector<uint64_t> input;
        if (type == "int64") {
            input = loadInt64File(filename, maxN);
        } else if (type == "int32") {
            input = loadInt32File(filename, maxN);
        } else {
            std::cerr<<"Unknown input type"<<std::endl;
            return 1;
        }
        if (input.size() < 2) {
            std::cerr<<"Input file does not contain integers"<<std::endl;
            return 1;
        }
        directRankStoringContenderRunner(input);

        std::cout<<"Converting to strings"<<std::endl;
        std::vector<std::string> inputAsString;
        inputAsString.reserve(input.size());
        for (uint64_t x : input) {
            uint64_t swapped = __builtin_bswap64(x);
            inputAsString.emplace_back((char*) &swapped, sizeof(uint64_t));
        }

        recursiveDirectRankStoringContenderRunner(inputAsString);
        centroidHollowTrieContenderRunner(inputAsString);
        hollowTrieContenderRunner(inputAsString);
        pathDecomposedTrieContenderRunner(inputAsString);
    }

    return 0;
}
