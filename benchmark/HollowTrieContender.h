#pragma once

#include "Contender.h"
#include <tries/hollow_trie.hpp>
#include <succinct/mapper.hpp>
#include <succinct/gamma_vector.hpp>
#include <succinct/elias_fano_list.hpp>

template <typename SkipsType>
class HollowTrieContender : public Contender {
    public:
        succinct::tries::hollow_trie<SkipsType> *mmphf = nullptr;

        HollowTrieContender() = default;

        ~HollowTrieContender() override = default;

        std::string name() override {
            return std::string("HollowTrie")
                    + " labelsPoolType=" + typeid(SkipsType).name();
        }

        void construct(const std::vector<std::string> &keys) override {
            mmphf = new succinct::tries::hollow_trie<SkipsType>(keys);
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

void hollowTrieContenderRunner(std::vector<std::string> &strings) {
    { HollowTrieContender<succinct::gamma_vector>().run(strings); }
    { HollowTrieContender<succinct::elias_fano_list>().run(strings); }
    { HollowTrieContender<succinct::mapper::mappable_vector<uint16_t>>().run(strings); }
}
