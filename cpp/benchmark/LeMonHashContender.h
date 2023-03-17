#pragma once

#include "Contender.h"
#include <LeMonHash.hpp>

class LeMonHashContender : public Contender<uint64_t> {
    public:
        lemonhash::LeMonHash<lemonhash::SuccinctPGMBucketMapper> *mmphf = nullptr;

        LeMonHashContender() = default;

        ~LeMonHashContender() override = default;

        std::string name() override {
            return std::string("LeMonHash");
        }

        void construct(const std::vector<uint64_t> &keys) override {
            mmphf = new lemonhash::LeMonHash<lemonhash::SuccinctPGMBucketMapper>(keys);
        }

        size_t sizeBits() override {
            return mmphf->spaceBits(false);
        }

        void performQueries(const std::vector<uint64_t> &keys) override {
            doPerformQueries(keys, *mmphf);
        }

        void performTest(const std::vector<uint64_t> &keys) override {
            doPerformTest(keys, *mmphf);
        }
};

void leMonHashContenderRunner(std::vector<uint64_t> &keys) {
    { LeMonHashContender().run(keys); }
}
