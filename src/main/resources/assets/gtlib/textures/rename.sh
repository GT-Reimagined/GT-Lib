#!/bin/sh
# check number of arguments
if [ $# -ne 2 ]; then
  echo "<<< ERROR: must have 2 arguments , but $# given "
  return 1
fi

for f in */; do
  mv "$f$1".png "$f$2".png
  mv "$f$1"_overlay.png "$f$2"_overlay.png
done
