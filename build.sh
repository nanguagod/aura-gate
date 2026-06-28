cd d:/alldesk/code/AuraGate && mvn install -DskipTests -Dmaven.clean.skip=true -pl auragate-rbac,auragate-ai -am > /tmp/mvn_build.log 2>&1
echo "EXIT=$?"