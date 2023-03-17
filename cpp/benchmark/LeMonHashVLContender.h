#pragma once

#include "Contender.h"
#include <LeMonHashVL.hpp>

class LeMonHashVLContender : public Contender<std::string> {
    public:
        lemonhash::LeMonHashVL *mmphf = nullptr;

        LeMonHashVLContender() = default;

        ~LeMonHashVLContender() override = default;

        std::string name() override {
            return std::string("LeMonHashVL");
        }

        void construct(const std::vector<std::string> &keys) override {
            mmphf = new lemonhash::LeMonHashVL(keys);
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

void leMonHashVLContenderRunner(std::vector<std::string> &strings) {
    { LeMonHashVLContender().run(strings); }
}
