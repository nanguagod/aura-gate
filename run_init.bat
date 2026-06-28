@echo off
"C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql" -u root -p123456 < "d:\alldesk\code\AuraGate\sql\auragate_rbac.sql"
echo %errorlevel%