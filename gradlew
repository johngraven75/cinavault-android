#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="9.5.0"
CACHE_ROOT="${HOME}/.cache/cinavault-gradle"
DIST_DIR="${CACHE_ROOT}/gradle-${GRADLE_VERSION}"
ZIP_PATH="${CACHE_ROOT}/gradle-${GRADLE_VERSION}-bin.zip"

if [[ ! -x "${DIST_DIR}/bin/gradle" ]]; then
  mkdir -p "${CACHE_ROOT}"
  if [[ ! -f "${ZIP_PATH}" ]]; then
    curl --fail --location --retry 4 \
      "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
      --output "${ZIP_PATH}"
  fi
  rm -rf "${DIST_DIR}"
  unzip -q "${ZIP_PATH}" -d "${CACHE_ROOT}"
fi

exec "${DIST_DIR}/bin/gradle" "$@"
