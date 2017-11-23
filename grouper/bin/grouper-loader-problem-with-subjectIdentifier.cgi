#!/bin/sh

echo "Content-type: text/plain"
echo ""

tac /usr/local/grouper/grouper/logs/grouper_error.log | sed -n 's/: .*ERROR .* Problem with subjectIdentifier: \([^,]*\), .*__\(.*\)__.*/ \2 \1/p'
