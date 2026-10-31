#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "Courier Shifts Automation setup"

if ! command -v java >/dev/null 2>&1; then
    echo "Java is not installed or is not on PATH."
    echo "Install JDK 25 (Java 25 or newer), then rerun this script."
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    if command -v sdk >/dev/null 2>&1; then
        echo "Installing Maven with SDKMAN..."
        sdk install maven 3.9.15
    elif [[ "$(uname -s)" == "Darwin" ]] && command -v brew >/dev/null 2>&1; then
        brew install maven
    elif command -v apt-get >/dev/null 2>&1; then
        sudo apt-get update
        sudo apt-get install -y maven
    else
        echo "Maven is required but could not be installed automatically."
        echo "Install Maven 3.9 or newer, then rerun this script."
        exit 1
    fi
fi

if command -v java >/dev/null 2>&1; then
    java -version
fi
mvn -version

echo "Downloading Maven dependencies..."
mvn dependency:go-offline

echo
echo "Setup complete."
echo "Install Docker/Chrome if needed, start the application separately, then run: mvn clean verify"
