#!/bin/bash
# Instala Ollama + modelo en el HOST de la VM (producción).
# El contenedor msvc-ia lo alcanza vía host.docker.internal (ya configurado
# en docker-compose.yml con extra_hosts). Ejecutar UNA vez en la VM:
#   bash scripts/instalar-ollama.sh
set -e

MODELO="${OLLAMA_MODEL:-qwen2.5-coder:7b}"

if ! command -v ollama >/dev/null 2>&1; then
  echo "== Instalando Ollama =="
  curl -fsSL https://ollama.com/install.sh | sh
else
  echo "== Ollama ya instalado =="
fi

echo "== Descargando modelo $MODELO (tarda varios minutos, ~4.7 GB) =="
ollama pull "$MODELO"

echo "== Verificando =="
curl -s http://localhost:11434/api/tags | grep -o '"name":"[^"]*"' | head -n 5

echo "OK. Ahora en la carpeta del proyecto: docker compose up -d msvc-ia"
