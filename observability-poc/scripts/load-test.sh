#!/usr/bin/env bash
# Generates traffic so the Grafana dashboard has data to show.
# Usage: ./scripts/load-test.sh [rounds]   (default 60)

BASE_URL="${BASE_URL:-http://localhost:8080}"
ROUNDS="${1:-60}"

for i in $(seq 1 "$ROUNDS"); do
  curl -s -o /dev/null -X POST "$BASE_URL/api/products" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"Item-$i\",\"price\":$((i * 10)),\"quantity\":$i}"
  curl -s -o /dev/null "$BASE_URL/api/products"
  curl -s -o /dev/null "$BASE_URL/api/products/$i"
  curl -s -o /dev/null "$BASE_URL/api/demo/slow"
  if [ $((i % 5)) -eq 0 ]; then
    curl -s -o /dev/null "$BASE_URL/api/demo/error"
  fi
  sleep 0.5
done

echo "Done. Open Grafana at http://localhost:3000"
