#!/bin/bash

if [[ -x "$(command -v mvn)" ]]; then
    mvn $@
elif [[ -f "mvnw" ]]; then
    ./mvnw $@
else
    echo "Maven is not installed locally or globally!";
fi