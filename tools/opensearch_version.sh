#!/bin/bash
# -----------------------------------------------------------------------------
# Script Name: opensearch_version.sh
#
# Description:
#   This script extracts the OpenSearch version from the build.gradle file of a
#   the notifications plugin directory. It searches for the 'opensearch_version' property
#   and parses its value, removing any '-SNAPSHOT' suffix if present.
#
# Usage:
#   ./opensearch_version.sh
#
# Output:
#   Prints the extracted OpenSearch version to stdout.
#
# Exit Codes:
#   0   Success
#   1   Missing argument or error during extraction
#
# Example:
#   ./opensearch_version.sh
# -----------------------------------------------------------------------------

file="notifications/build.gradle"

# Extract the OpenSearch version
version=$(grep "opensearch_version =" "${file}" |
    sed -E 's/.*System.getProperty\("opensearch\.version", "//' |
    sed -E 's/".*//' |
    sed -E 's/-SNAPSHOT$//')

echo "${version}"
exit 0