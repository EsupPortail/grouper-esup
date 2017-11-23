#!/bin/sh

GROUP="$QUERY_STRING"

echo "Content-type: text/plain"
echo ""

echo "Group to update: $GROUP"
echo
sudo -u grouper /usr/local/grouper/grouper/bin/sync-grouper-loader-group-and-export-to-LDAP "$GROUP"
