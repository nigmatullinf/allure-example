#!/usr/bin/env bash
set -euo pipefail

: "${GITHUB_API_TOKEN:?Missing GITHUB_API_TOKEN}"
: "${GITHUB_REPOSITORY:?Missing GITHUB_REPOSITORY (owner/repo)}"

WORKFLOW_FILE="${WORKFLOW_FILE:-gradle.yml}"
GITHUB_REF="${GITHUB_REF:-master}"

json_escape() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

inputs="{"
add_input() {
  local key="$1"
  local value="$2"
  if [ -n "$value" ]; then
    if [ "$inputs" != "{" ]; then
      inputs+=", "
    fi
    inputs+="\"${key}\":\"$(json_escape "$value")\""
  fi
}

add_input "TEST_ENDPOINT" "${TEST_ENDPOINT:-}"
add_input "TEST_BROWSER" "${TEST_BROWSER:-}"
add_input "ALLURE_JOB_RUN_ID" "${ALLURE_JOB_RUN_ID:-}"
add_input "ALLURE_USERNAME" "${ALLURE_USERNAME:-}"
inputs+="}"

payload="{\"ref\":\"$(json_escape "$GITHUB_REF")\",\"inputs\":${inputs}}"

curl -sS -X POST \
  -H "Authorization: Bearer ${GITHUB_API_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "Content-Type: application/json" \
  -d "${payload}" \
  "https://api.github.com/repos/${GITHUB_REPOSITORY}/actions/workflows/${WORKFLOW_FILE}/dispatches"
