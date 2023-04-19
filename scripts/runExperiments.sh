#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters."
    echo "Usage: runExperiments.sh pathToDatasetsFolder pathToCppBinary pathToJarFile"
    exit 1
fi

pathToDatasetsFolder=$1
pathToCppBinary=$2
pathToJarFile=$3

function benchmarkDataset() {
    $pathToCppBinary $@ --numQueries 5M
    java -Xmx64G -jar $pathToJarFile $@ --numQueries 5000000
}

hostname
strings $pathToCppBinary | grep " -m"

benchmarkDataset --type strings --filename "$pathToDatasetsFolder/dna-31-mer.txt"
benchmarkDataset --type strings --filename "$pathToDatasetsFolder/trec-text.terms"
benchmarkDataset --type strings --filename "$pathToDatasetsFolder/uk-2007-05.urls"

benchmarkDataset --type int32 --filename "$pathToDatasetsFolder/5GRAM_1"
benchmarkDataset --type int64 --filename "$pathToDatasetsFolder/fb_200M_uint64"
benchmarkDataset --type int64 --filename "$pathToDatasetsFolder/osm_cellids_800M_uint64"

benchmarkDataset --type int64 --filename "$pathToDatasetsFolder/uniform_uint64"
benchmarkDataset --type int64 --filename "$pathToDatasetsFolder/exponential_uint64"
benchmarkDataset --type int64 --filename "$pathToDatasetsFolder/normal_uint64"

