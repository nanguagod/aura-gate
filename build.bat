cd /d d:\alldesk\code\AuraGate
call mvn install -DskipTests -Dmaven.clean.skip=true -pl auragate-rbac,auragate-ai -am > C:\Users\nangua\mvn_build.log 2>&1
echo EXIT=%errorlevel%