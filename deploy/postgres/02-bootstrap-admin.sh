#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${BOOTSTRAP_ADMIN_PASSWORD_HASH:-}" ]]; then
    exit 0
fi
if [[ ! "$BOOTSTRAP_ADMIN_PASSWORD_HASH" =~ ^\$2[aby]\$(0[4-9]|[12][0-9]|3[01])\$[./A-Za-z0-9]{53}$ ]]; then
    echo 'BOOTSTRAP_ADMIN_PASSWORD_HASH must be a BCrypt hash.' >&2
    exit 1
fi

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\getenv admin_email BOOTSTRAP_ADMIN_EMAIL
\getenv password_hash BOOTSTRAP_ADMIN_PASSWORD_HASH
INSERT INTO admin_user (email, password_hash, display_name, role)
VALUES (lower(trim(:'admin_email')), :'password_hash', '개발 운영자', 'REPRESENTATIVE')
ON CONFLICT (email) DO NOTHING;
SQL
