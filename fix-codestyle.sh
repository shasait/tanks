#!/bin/bash

grep -l -r "@Nonnull final" --include '*.java' | while read f; do echo "@Nonnull final - $f"; sed -i -e 's/@Nonnull final/final @Nonnull/g' "$f"; done
grep -l -r "@Nullable final" --include '*.java' | while read f; do echo "@Nullable final - $f"; sed -i -e 's/@Nullable final/final @Nullable/g' "$f"; done

