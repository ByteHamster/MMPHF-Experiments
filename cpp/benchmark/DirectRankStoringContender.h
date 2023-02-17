#pragma once

#include "Contender.h"
#include <DirectRankStoring.hpp>

class DirectRankStoringContender : public Contender<uint64_t> {
    public:
        DirectRankStoringMmphf<SuccinctPGMBucketMapper> *mmphf = nullptr;

        DirectRankStoringContender() = default;

        ~DirectRankStoringContender() override = default;

        std::string name() override {
            return std::string("DirectRankStoring");
        }

        void construct(const std::vector<uint64_t> &keys) override {
            mmphf = new DirectRankStoringMmphf<SuccinctPGMBucketMapper>(keys);
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

void directRankStoringContenderRunner(std::vector<uint64_t> &keys) {
    { DirectRankStoringContender().run(keys); }
}
