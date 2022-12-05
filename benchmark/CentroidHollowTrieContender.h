#pragma once

#include "Contender.h"
#include <tries/centroid_hollow_trie.hpp>
#include <succinct/mapper.hpp>

class CentroidHollowTrieContender : public Contender {
    public:
        succinct::tries::centroid_hollow_trie *mmphf;

        CentroidHollowTrieContender() {
        }

        ~CentroidHollowTrieContender() override {
        }

        std::string name() override {
            return std::string("CentroidHollowTrie");
        }

        void construct(const std::vector<std::string> &keys) override {
            mmphf = new succinct::tries::centroid_hollow_trie(keys);
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

void centroidHollowTrieContenderRunner(std::vector<std::string> &strings) {
    { CentroidHollowTrieContender().run(strings); }
}
