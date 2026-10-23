#!/bin/sh
set -eu

# Accept Windows line endings, blank lines and comments in the bucket list.
buckets=$(awk '
  { sub(/\r$/, ""); sub(/#.*/, ""); gsub(/^[ \t]+|[ \t]+$/, "") }
  NF { printf "%s%s", separator, $0; separator="," }
' /dev-tools/buckets.txt)

exec /entrypoint.sh "$@" "-bucket=$buckets"
