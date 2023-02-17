#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters."
    echo "Usage: runExperiments.sh pathToDatasetsFolder pathToCppBinary pathToJarFile"
    exit 1
fi

hostname

pathToDatasetsFolder=$1
pathToCppBinary=$2
pathToJarFile=$3

function benchmarkDataset() {
    $pathToCppBinary $@ --numQueries 10M
    java -jar $pathToJarFile $@ --numQueries 10000000
}

benchmarkDataset --type strings --filename "$pathToDatasetsFolder/trec-title.terms"
benchmarkDataset --type strings --filename "$pathToDatasetsFolder/trec-text.terms"
benchmarkDataset --type strings --filename "$pathToDatasetsFolder/uk-2007-05.urls"

benchmarkDataset --type integers --filename "$pathToDatasetsFolder/books_800M_uint64"
benchmarkDataset --type integers --filename "$pathToDatasetsFolder/fb_200M_uint64"
benchmarkDataset --type integers --filename "$pathToDatasetsFolder/osm_cellids_800M_uint64"

