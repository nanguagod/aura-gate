#!/bin/bash
kill -9 390 2>/dev/null
sleep 2
cd d:/alldesk/code/AuraGate/auragate-ai
mvn spring-boot:run -Dspring-boot.run.profiles=rbac,ai > /tmp/auragate.log 2>&1 &
echo "Started PID=$!"