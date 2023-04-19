#!/bin/bash
hostname

/opt/mmphf/cpp/build/GenerateData -n 30M --type normal --filename /opt/mmphf/normal_uint64

cd /opt/mmphf/cpp/build
strings ./Comparison | grep " -m" | tee /opt/dockerVolume/normal-distribution.txt
./Comparison --type int64 --filename /opt/mmphf/normal_uint64 --numQueries 5M | tee --append /opt/dockerVolume/normal-distribution.txt

java -Xmx64G -jar /opt/mmphf/java/target/MmphfExperiments-1.0-jar-with-dependencies.jar --type int64 --filename /opt/mmphf/normal_uint64 --numQueries 5000000 | tee --append /opt/dockerVolume/normal-distribution.txt

# Build plot
cd /opt/dockerVolume
/opt/sqlplot-tools/build/src/sqlplot-tools normal-distribution.tex
rm -f normal-distribution.pdf
pdflatex normal-distribution.tex
pdflatex normal-distribution.tex
rm -f *.out *.log *.aux

