#pragma once

#include <random>
#include <iostream>
#include <chrono>
#include <XorShift64.h>
#include <unistd.h>
#include "BenchmarkData.h"

#define DO_NOT_OPTIMIZE(value) asm volatile ("" : : "r,m"(value) : "memory")

class Contender {
    public:
        static size_t numQueries;
        static std::string dataset;

        Contender() {
        }

        virtual ~Contender() = default;

        virtual std::string name() = 0;
        virtual size_t sizeBits() = 0;
        virtual void construct(const std::vector<std::string> &keys) = 0;

        virtual void beforeConstruction(const std::vector<std::string> &keys) {
            (void) keys;
        }
        virtual void performQueries(const std::vector<std::string> &keys) = 0;
        virtual void performTest(const std::vector<std::string> &keys) = 0;

        void run(std::vector<std::string> keys) {
            size_t N = keys.size();
            std::cout << std::endl;
            std::cout << "Contender: " << name().substr(0, name().find(' ')) << std::endl;
            beforeConstruction(keys);

            std::cout << "Cooldown" << std::endl;
            usleep(1000*1000);
            std::cout << "Constructing" << std::endl;

            std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();
            try {
                construct(keys);
            } catch (const std::exception& e) {
                std::cout<<"Error: "<<e.what()<<std::endl;
                return;
            }
            std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
            long constructionTime = std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count();

            std::cout<<"Testing"<<std::endl;
            performTest(keys);

            long queryTime = 0;
            if (numQueries > 0) {
                std::cout<<"Preparing query plan"<<std::endl;
                std::vector<std::string> queryPlan;
                queryPlan.reserve(numQueries);
                util::XorShift64 prng(time(nullptr));
                for (size_t i = 0; i < numQueries; i++) {
                    queryPlan.push_back(keys[prng(N)]);
                }
                std::cout << "Cooldown" << std::endl;
                usleep(1000*1000);
                std::cout<<"Querying"<<std::endl;
                begin = std::chrono::steady_clock::now();
                performQueries(queryPlan);
                end = std::chrono::steady_clock::now();
                queryTime = std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count();
            }
            double bitsPerElement = (double) sizeBits() / N;
            std::cout << "RESULT"
                      << " dataset=" << dataset
                      << " name=" << name()
                      << " bitsPerElement=" << bitsPerElement
                      << " constructionTimeMilliseconds=" << constructionTime
                      << " queryTimeMilliseconds=" << queryTime
                      << " numQueries=" << numQueries
                      << " N=" << N
                      << std::endl;
        }
    protected:
        template<typename F>
        void doPerformQueries(const std::vector<std::string> &keys, F &hashFunction) {
            for (const std::string &key : keys) {
                // Some contenders expect non-const keys but actually use them as const.
                size_t retrieved = hashFunction(const_cast<std::string &>(key));
                DO_NOT_OPTIMIZE(retrieved);
            }
        }

        template<typename F>
        void doPerformTest(const std::vector<std::string> &keys, F &hashFunction) {
            for (size_t i = 0; i < keys.size(); i++) {
                // Some contenders expect non-const keys but actually use them as const.
                size_t retrieved = hashFunction(const_cast<std::string &>(keys[i]));
                if (retrieved != i) {
                    std::cout << "Error: Key at index "<<i<<" is not monotone minimal perfect (output: "<<retrieved<<")"<< std::endl;
                    throw std::logic_error("Not MMPHF");
                }
            }
        }
};
size_t Contender::numQueries = 1e5;
std::string Contender::dataset = "unknown";
