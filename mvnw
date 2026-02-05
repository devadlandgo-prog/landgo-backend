#!/bin/sh
# Maven Wrapper script - downloads and runs Maven
# This is a simplified version - for production, use the official maven-wrapper

set -e

# Try to find Maven
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "Maven not found. Please install Maven or set up JAVA_HOME and download the Maven wrapper."
    echo "You can install Maven via: brew install maven"
    exit 1
fi
