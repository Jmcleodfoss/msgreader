#!/bin/bash
# Run directly from the extras directory
grep -R --include=*.java "import.*javafx" .. | sed "/^.*import \(.*\)\..*;$/s//\1/" | sort -u > javafx-package-list/package-list
