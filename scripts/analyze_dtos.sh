#!/usr/bin/env bash
# Script to list all DTO classes and their current @Schema annotations

echo "==== DTO Classes ===="
fd ".*DTO\.java$" --type f

echo
echo "==== Existing @Schema annotations in DTOs ===="
grep -R --include="*DTO.java" "@Schema" -C3 src/main/java
