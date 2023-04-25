#pragma once

#include "../Contender.h"
#include <SicHash.h>

template<typename Type>
class SicHashContender : public Contender<Type> {
    public:
        sichash::SicHash<true> *perfectHashing = nullptr;
        sichash::SicHashConfig config;

        SicHashContender(sichash::SicHashConfig config) : config(config) {
        }

        ~SicHashContender() override {
            delete perfectHashing;
        }

        std::string name() override {
            return std::string("SicHash monotone=0");
        }

        void construct(const std::vector<std::string> &keys) override {
            perfectHashing = new sichash::SicHash<true>(keys, config);
        }

        size_t sizeBits() override {
            return perfectHashing->spaceUsage();
        }

        void performQueries(const std::vector<std::string> &keys) override {
            this->doPerformQueries(keys, *perfectHashing);
        }

        void performTest(const std::vector<std::string> &keys) override {
            std::cout << "Ignoring test, method is non-monotone" << std::endl;
        }
};

template <typename Type>
void sicHashContenderRunner(std::vector<Type> &keys) {
    { SicHashContender<Type>(sichash::SicHashConfig().spaceBudget(2.2, 0.0)).run(keys); }
}
