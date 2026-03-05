#!/bin/bash
# -----------------------------------------------------------------------------
# Script Name: product_version.sh
#
# Description:
#   This script retrieves and prints the product version from the VERSION.json
#   file located one directory above the script's location. It uses the 'jq'
#   utility to parse the JSON file and extract the 'version' field.
#
# Usage:
#   ./product_version.sh
#
# Requirements:
#   - jq must be installed and available at /usr/bin/jq
#
# Exit Codes:
#   0 - Success
#   1 - Failure (jq not found or directory change failed)
#
# -----------------------------------------------------------------------------

## Check that jq is installed
if [ ! -e '/usr/bin/jq' ]; then
    echo "ERROR: jq command could not be found under /usr/bin"
    exit 1
fi

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

jq -r .version ../VERSION.json

exit 0