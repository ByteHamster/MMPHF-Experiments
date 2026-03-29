#!/bin/bash
hostname

/opt/mmphf/cpp/build/GenerateData -n 30M --type uniform --filename /opt/mmphf/uniform_uint64

cd /opt/mmphf/cpp/build
strings ./Comparison | grep " -m" | tee /opt/dockerVolume/uniform-distribution.txt
./Comparison --type int64 --filename /opt/mmphf/uniform_uint64 --numQueries 5M | tee --append /opt/dockerVolume/uniform-distribution.txt

java -Xmx64G -jar /opt/mmphf/java/target/MmphfExperiments-1.0-jar-with-dependencies.jar --type int64 --filename /opt/mmphf/uniform_uint64 --numQueries 5000000 | tee --append /opt/dockerVolume/uniform-distribution.txt

# Build plot
cd /opt/dockerVolume
/opt/sqlplot-tools/build/src/sqlplot-tools uniform-distribution.tex
rm -f uniform-distribution.pdf
pdflatex uniform-distribution.tex
pdflatex uniform-distribution.tex
rm -f *.out *.log *.aux

