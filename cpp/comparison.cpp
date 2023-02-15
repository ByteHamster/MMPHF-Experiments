#include <tlx/cmdline_parser.hpp>
#include "benchmark/DirectRankStoringContender.h"
#include "benchmark/CentroidHollowTrieContender.h"
#include "benchmark/HollowTrieContender.h"
#include "benchmark/PathDecomposedTrieContender.h"

int main(int argc, char** argv) {
    size_t maxN = std::numeric_limits<size_t>::max();
    std::string filename;

    tlx::CmdlineParser cmd;
    cmd.add_bytes('n', "maxN", maxN, "Truncate input file to only this number of strings if it is longer");
    cmd.add_bytes('q', "numQueries", Contender::numQueries, "Number of queries to perform");
    cmd.add_string('f', "filename", filename, "Input data set to load. List of strings, separated by newlines");

    if (!cmd.process(argc, argv)) {
        return 1;
    }

    std::vector<std::string> input = loadFile(filename, maxN);
    if (input.size() < 2) {
        std::cerr<<"Input file does not contain strings"<<std::endl;
        return 1;
    }

    size_t positionOfSlash = filename.find_last_of('/');
    Contender::dataset = positionOfSlash == std::string::npos ? filename : filename.substr(positionOfSlash + 1);

    directRankStoringContenderRunner(input);
    centroidHollowTrieContenderRunner(input);
    hollowTrieContenderRunner(input);
    pathDecomposedTrieContenderRunner(input);

    return 0;
}
