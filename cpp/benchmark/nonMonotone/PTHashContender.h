#pragma once

#include "../Contender.h"
#include <pthash.hpp>

template <typename Type>
class PTHashContender : public Contender<Type> {
    public:
        pthash::single_phf<pthash::murmurhash2_64, pthash::elias_fano, true> pthashFunction;

        PTHashContender() = default;

        ~PTHashContender() override = default;

        std::string name() override {
            return std::string("PTHash monotone=0");
        }

        void beforeConstruction(const std::vector<Type> &keys) override {
            (void) keys;
        }

        void construct(const std::vector<Type> &keys) override {
            pthash::build_configuration config;
            config.c = 10.0;
            config.alpha = 0.96;
            config.num_threads = 1;
            config.minimal_output = true;
            config.verbose_output = false;
            pthashFunction.build_in_internal_memory(keys.begin(), keys.size(), config);
        }

        size_t sizeBits() override {
            return pthashFunction.num_bits();
        }

        void performQueries(const std::vector<Type> &keys) override {
            this->doPerformQueries(keys, pthashFunction);
        }

        void performTest(const std::vector<Type> &keys) override {
            std::cout << "Ignoring test, method is non-monotone" << std::endl;
        }
};

template <typename Type>
void ptHashContenderRunner(std::vector<Type> &keys) {
    { PTHashContender<Type>().run(keys); }
}
