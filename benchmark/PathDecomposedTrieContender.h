#pragma once

#include "Contender.h"
#include <tries/path_decomposed_trie.hpp>
#include <succinct/mapper.hpp>
#include <tries/vbyte_string_pool.hpp>
#include <tries/compressed_string_pool.hpp>

template <typename LabelsPoolType>
class PathDecomposedTrieContender : public Contender {
    public:
        succinct::tries::path_decomposed_trie<LabelsPoolType, true> *mmphf = nullptr;

        PathDecomposedTrieContender() = default;

        ~PathDecomposedTrieContender() override = default;

        std::string name() override {
            return std::string("PathDecomposedTrie")
                    + " labelsPoolType=" + typeid(LabelsPoolType).name();
        }

        void construct(const std::vector<std::string> &keys) override {
            mmphf = new succinct::tries::path_decomposed_trie<LabelsPoolType, true>(keys);
        }

        size_t sizeBits() override {
            succinct::mapper::detail::sizeof_visitor v(true);
            v(*mmphf, "mmphf");
            return 8 * v.size();
        }

        void performQueries(const std::vector<std::string> &keys) override {
            auto x = [&] (std::string &key) {
                return mmphf->index(key);
            };
            doPerformQueries(keys, x);
        }

        void performTest(const std::vector<std::string> &keys) override {
            auto x = [&] (std::string &key) {
                return mmphf->index(key);
            };
            doPerformTest(keys, x);
        }
};

void pathDecomposedTrieContenderRunner(std::vector<std::string> &strings) {
    { PathDecomposedTrieContender<succinct::tries::vbyte_string_pool>().run(strings); }
    { PathDecomposedTrieContender<succinct::tries::compressed_string_pool>().run(strings); }
}
