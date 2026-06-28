#!/bin/bash
# kill all java/maven processes
kill -9 $(ps aux | grep -E "java|mvn" | grep -v grep | awk '{print $1}') 2>/dev/null
sleep 3
rm -f /tmp/auragate.log
echo "cleaned"