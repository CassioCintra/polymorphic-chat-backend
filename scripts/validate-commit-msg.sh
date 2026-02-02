#!/bin/sh

COMMIT_MSG_FILE="${1:-.git/COMMIT_EDITMSG}"

if [ ! -f "$COMMIT_MSG_FILE" ]; then
  echo "❌ Commit message file not found: $COMMIT_MSG_FILE"
  exit 1
fi

COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

PATTERN="^(feat|fix|chore|docs|refactor|test|ci)(\\(.+\\))?: .{5,}"

if ! echo "$COMMIT_MSG" | grep -Eq "$PATTERN"; then
  echo "❌ Invalid commit message."
  echo ""
  echo "Use a pattern like:"
  echo "  feat: add websocket authentication"
  echo "  fix(ci): fix pipeline trigger"
  echo "  chore: bump version"
  echo ""
  exit 1
fi
