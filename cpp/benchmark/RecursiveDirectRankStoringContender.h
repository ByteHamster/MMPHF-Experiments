#pragma once

#include "Contender.h"
#include <RecursiveDirectRankStoring.hpp>

class RecursiveDirectRankStoringContender : public Contender<std::string> {
    public:
        RecursiveDirectRankStoringMmphf *mmphf = nullptr;

        RecursiveDirectRankStoringContender() = default;

        ~RecursiveDirectRankStoringContender() override = default;

        std::string name() override {
            return std::string("RecursiveDirectRankStoring");
        }

        void construct(const std::vector<std::string> &keys) override {
            mmphf = new RecursiveDirectRankStoringMmphf(keys);
        }

        size_t sizeBits() override {
            return mmphf->spaceBits();
        }

        void performQueries(const std::vector<std::string> &keys) override {
            doPerformQueries(keys, *mmphf);
        }

        void performTest(const std::vector<std::string> &keys) override {
            doPerformTest(keys, *mmphf);
        }
};

void recursiveDirectRankStoringContenderRunner(std::vector<std::string> &strings) {
    { RecursiveDirectRankStoringContender().run(strings); }
}
