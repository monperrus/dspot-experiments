#!/usr/bin/env bash

project=$1

ls

./src/main/bash/install_dspot.sh

~/apache-maven-3.3.9/bin/mvn package

python src/main/python/october-2017/mutation_analysis.py ${project} > src/main/bash/run-mutant-original.sh
python src/main/python/october-2017/amplification.py ${project} > src/main/bash/amplification.sh
python src/main/python/october-2017/mutation_analysis.py ${project} amplified > src/main/bash/run-mutant-amplified.sh
