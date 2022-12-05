#pragma once

#include "Contender.h"
#include <RecursiveDirectRankStoring.hpp>

class DirectRankStoringContender : public Contender {
    public:
       RecursiveDirectRankStoringMmphf *mmphf;

        DirectRankStoringContender() {
        }

        ~DirectRankStoringContender() override {
        }

        std::string name() override {
            return std::string("DirectRankStoring");
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

void directRankStoringContenderRunner(std::vector<std::string> &strings) {
    { DirectRankStoringContender().run(strings); }
}
