#!/bin/bash

if ! command -v mvn &> /dev/null; then
    echo "Maven meant to be installed but it's not. Please install Maven first."
    exit 1
fi

echo "Building the project..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful! Running the application..."
    java -jar target/BetterTown-1.0-SNAPSHOT-exec.jar
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi
