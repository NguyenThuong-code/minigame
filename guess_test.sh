#!/usr/bin/env bash
set -euo pipefail

# ---------- CONFIG (override with env) ----------
BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:-eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ2aWRDa2tNb2tlUDhKa1VTZ25SN0JYZ0kzNzB0LUNraFM1NmQ4N0hpLWl3In0.eyJleHAiOjE3NzIyNTgyNDMsImlhdCI6MTc3MjI1Nzk0MywianRpIjoiZDM1YzkyNGQtMGNmMi00M2QyLWIxZDEtYzE4ZmM5NTdjYjYyIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgzL3JlYWxtcy9taW5pZ2FtZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlNzljYzFhNy01ZGEwLTQ1MTMtYjFjNS1iMjE4NzlmYTFjZGUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtaW5pLWdhbWUiLCJzZXNzaW9uX3N0YXRlIjoiNTRkM2QwODktNjIwNS00YjAwLTlkZTUtYzI2ODhkMGM0OGRiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtbWluaWdhbWUiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiVVNFUiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6IjU0ZDNkMDg5LTYyMDUtNGIwMC05ZGU1LWMyNjg4ZDBjNDhkYiIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoidGh1b25nIG5ndXllbiIsInByZWZlcnJlZF91c2VybmFtZSI6InRodW9uZ252MSIsImdpdmVuX25hbWUiOiJ0aHVvbmciLCJmYW1pbHlfbmFtZSI6Im5ndXllbiIsImVtYWlsIjoidGh1b25nbnYxQGdtYWlsLmNvbSJ9.TNHFG30Uqj-QOwUtxCuOkAZJoMbk8u-QiuK4G9TpBugTS37cunLSnT_JYYYb4EWMSMJsq4PfgP9bqsSY9ZH_lgsUJRPVeJSMW96-LgzAJAGtt5GcMP4LSAsFQS9Xs04N4Gu3cRiOwajQNL5-GvY3G8wjCPeE2T8WqI-_wcmeis9wraEFI2DzpdqWk1-ORnZlcHFV3mExge-v4rhUbXHb_TKFo8KfGw72ETOJSecyQ9m7DdwNLDwlkTk7JBKf6e8oBVsUAyTEofbJw4KvvzS0_av_hJ8eOvgHnjnS30X9M2R0Bdf2aXv6hxcaIYALywX0wfAFzT7AQ6_vneQalO-K6g}"
GUESS_NUMBER="${GUESS_NUMBER:-3}"       # 1..5
BUY_TIMES="${BUY_TIMES:-5}"            # each buy adds +5 turns
TOTAL_REQUESTS="${TOTAL_REQUESTS:-100}"
CONCURRENCY="${CONCURRENCY:-20}"
ENDPOINT_GUESS="${ENDPOINT_GUESS:-/api/game/guess}"
ENDPOINT_BUY="${ENDPOINT_BUY:-/api/game/buy-turns}"
OUTDIR="${OUTDIR:-./guess_out}"

# ---------- HELPERS ----------
need() { command -v "$1" >/dev/null 2>&1; }

# ---------- CHECKS ----------
if [[ -z "$TOKEN" ]]; then
  echo "ERROR: TOKEN is empty."
  echo "Usage (Git Bash):"
  echo '  TOKEN="eyJ..." BASE_URL="http://localhost:8080" bash guess_test.sh'
  exit 1
fi

mkdir -p "$OUTDIR"
RESULTS_FILE="$OUTDIR/results.jsonl"
: > "$RESULTS_FILE"

AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "=== CONFIG ==="
echo "BASE_URL       = $BASE_URL"
echo "GUESS_NUMBER   = $GUESS_NUMBER"
echo "BUY_TIMES      = $BUY_TIMES"
echo "TOTAL_REQUESTS = $TOTAL_REQUESTS"
echo "CONCURRENCY    = $CONCURRENCY"
echo "OUTDIR         = $OUTDIR"
echo

# ---------- STEP 1: BUY TURNS ----------
echo "==> Step 1: Buy turns ($BUY_TIMES times)"
for i in $(seq 1 "$BUY_TIMES"); do
  resp="$(curl -sS -X POST "$BASE_URL$ENDPOINT_BUY" -H "$AUTH_HEADER" || true)"
  echo "Buy #$i: $resp"
done
echo

# ---------- STEP 2: CONCURRENT GUESS ----------
echo "==> Step 2: Concurrent guess ($TOTAL_REQUESTS requests, concurrency=$CONCURRENCY)"
echo "Results file: $RESULTS_FILE"
echo "Response bodies saved to: $OUTDIR/resp_<n>.txt"
echo

do_one_guess() {
  local idx="$1"
  local body
  body=$(printf '{"number":%s}' "$GUESS_NUMBER")

  local tmp="$OUTDIR/resp_$idx.txt"
  local hdr="$OUTDIR/hdr_$idx.txt"

  # save headers + body (for debugging)
  local code
  code=$(curl -sS -D "$hdr" -o "$tmp" -w "%{http_code}" \
    -X POST "$BASE_URL$ENDPOINT_GUESS" \
    -H "$AUTH_HEADER" \
    -H "Content-Type: application/json" \
    -d "$body" || true)

  [[ -z "${code:-}" ]] && code="000"

  local raw
  raw="$(cat "$tmp" 2>/dev/null || true)"

  # If response body is JSON, store it as object in "body"
  if need jq && jq -e . "$tmp" >/dev/null 2>&1; then
    echo "{\"i\":$idx,\"http\":$code,\"body\":$raw}" >> "$RESULTS_FILE"
    return 0
  fi

  # Not JSON (or jq missing) -> store raw as string safely (if jq exists)
  if need jq; then
    raw_escaped=$(printf "%s" "$raw" | jq -Rs .)
    echo "{\"i\":$idx,\"http\":$code,\"raw\":$raw_escaped}" >> "$RESULTS_FILE"
  else
    # fallback without jq: at least store length so you know it returned something
    echo "{\"i\":$idx,\"http\":$code,\"len\":${#raw}}" >> "$RESULTS_FILE"
  fi
}

export -f do_one_guess
export BASE_URL ENDPOINT_GUESS AUTH_HEADER GUESS_NUMBER OUTDIR RESULTS_FILE

seq 1 "$TOTAL_REQUESTS" | xargs -I{} -P "$CONCURRENCY" bash -lc 'do_one_guess "$@"' _ {}

echo
echo "==> Step 3: Summary"
echo

# ---------- STEP 3: SUMMARY ----------
if need jq; then
  total=$(wc -l < "$RESULTS_FILE" | tr -d ' ')
  ok200=$(jq -s '[.[] | select(.http==200)] | length' "$RESULTS_FILE")
  http409=$(jq -s '[.[] | select(.http==409)] | length' "$RESULTS_FILE")
  http401=$(jq -s '[.[] | select(.http==401)] | length' "$RESULTS_FILE")
  http400=$(jq -s '[.[] | select(.http==400)] | length' "$RESULTS_FILE")
  http000=$(jq -s '[.[] | select(.http==0 or .http==000)] | length' "$RESULTS_FILE")

  # wins/loses only if "body.correct" exists
  wins=$(jq -s '[.[] | select(.http==200 and (.body.correct==true))] | length' "$RESULTS_FILE")
  loses=$(jq -s '[.[] | select(.http==200 and (.body.correct==false))] | length' "$RESULTS_FILE")

  echo "Total : $total"
  echo "200   : $ok200"
  echo "Wins  : $wins"
  echo "Loses : $loses"
  echo "409   : $http409"
  echo "401   : $http401"
  echo "400   : $http400"
  echo "000   : $http000"
  echo
  echo "Sample (first 5 HTTP 200):"
  jq -c 'select(.http==200) | {i,http,correct:(.body.correct),remainingTurns:(.body.remainingTurns),currentScore:(.body.currentScore),message:(.body.message)}' \
    "$RESULTS_FILE" | head -n 5
else
  total=$(wc -l < "$RESULTS_FILE" | tr -d ' ')
  ok200=$(grep -c '"http":200' "$RESULTS_FILE" || true)
  http409=$(grep -c '"http":409' "$RESULTS_FILE" || true)
  echo "Total : $total"
  echo "200   : $ok200"
  echo "409   : $http409"
  echo "Tip: install jq for detailed stats (wins/loses)."
fi

echo
echo "DONE."
echo "Raw results: $RESULTS_FILE"
echo "Bodies: $OUTDIR/resp_<n>.txt"