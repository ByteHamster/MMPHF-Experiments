#!/bin/bash
hostname

/opt/mmphf/cpp/build/GenerateData -n 30M --type exponential --filename /opt/mmphf/exponential_uint64

cd /opt/mmphf/cpp/build
strings ./Comparison | grep " -m" | tee /opt/dockerVolume/exponential-distribution.txt
./Comparison --type int64 --filename /opt/mmphf/exponential_uint64 --numQueries 5M | tee --append /opt/dockerVolume/exponential-distribution.txt

java -Xmx64G -jar /opt/mmphf/java/target/MmphfExperiments-1.0-jar-with-dependencies.jar --type int64 --filename /opt/mmphf/exponential_uint64 --numQueries 5000000 | tee --append /opt/dockerVolume/exponential-distribution.txt

# Build plot
cd /opt/dockerVolume
/opt/sqlplot-tools/build/src/sqlplot-tools exponential-distribution.tex
rm -f exponential-distribution.pdf
pdflatex exponential-distribution.tex
pdflatex exponential-distribution.tex
rm -f *.out *.log *.aux

