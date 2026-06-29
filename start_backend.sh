#!/bin/bash
# Read DeepSeek API key from Windows user env var
API_KEY=$(powershell -Command "[System.Environment]::GetEnvironmentVariable('aura-gate-apikey', 'User')")
echo "API Key found: ${API_KEY:0:8}..."
export SPRING_AI_OPENAI_API_KEY="$API_KEY"
cd /d/alldesk/code/AuraGate
java -jar auragate-ai/target/auragate-ai-1.0.0.jar
