#!/usr/bin/env bash
# Review Topper API — curl examples (paste into terminal or Postman “Import raw text”)
# Usage: export BASE_URL=http://localhost:8080 TOKEN=your_jwt_here

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:-}"

hdr_auth=(-H "Authorization: Bearer ${TOKEN}")

# -----------------------------------------------------------------------------
# AUTH — no JWT unless noted
# -----------------------------------------------------------------------------

### Register — creates USER + Starter subscription; returns JWT in body
curl -sS -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Owner","email":"jane@example.com","phone":"+15555550100","password":"SecurePass123"}'

### Login — returns JWT (copy token.value into TOKEN)
curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"SecurePass123"}'

### Forgot password — generic response; plaintext token only if EXPOSE_RESET_TOKEN=true on server
curl -sS -X POST "$BASE_URL/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com"}'

### Reset password — plain reset token from email/dev flag
curl -sS -X POST "$BASE_URL/api/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{"resetToken":"PASTE_TOKEN","newPassword":"NewSecurePass123"}'

### Me — current user profile [JWT]
curl -sS "$BASE_URL/api/auth/me" "${hdr_auth[@]}"

### Change password — authenticated [JWT]
curl -sS -X POST "$BASE_URL/api/auth/change-password" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"currentPassword":"SecurePass123","newPassword":"AnotherPass456"}'

# -----------------------------------------------------------------------------
# PUBLIC — guest flows (no JWT)
# -----------------------------------------------------------------------------

### Landing JSON — tracks PAGE_VISIT; returns branding + labels + whatsappDeepLink
curl -sS "$BASE_URL/r/demo-wellness-co?locale=en"

### Redirect — 302 to Google review URL; tracks redirect metric
curl -sS -o /dev/null -w "%{http_code}\n" -L "$BASE_URL/redirect/demo-wellness-co"

### Feedback — public submission
curl -sS -X POST "$BASE_URL/api/feedback" \
  -H "Content-Type: application/json" \
  -d '{"workspaceSlug":"demo-wellness-co","message":"Private feedback text","sentiment":"NEGATIVE","visitorSubmissionKey":"00000000-0000-4000-8000-000000000001"}'

### Active subscription plans — public pricing catalog
curl -sS "$BASE_URL/api/subscription-plans"

# -----------------------------------------------------------------------------
# WORKSPACES — owner JWT required
# -----------------------------------------------------------------------------

WORKSPACE_ID="${WORKSPACE_ID:-1}"

### List my workspaces
curl -sS "$BASE_URL/api/workspaces" "${hdr_auth[@]}"

### Create workspace — slug optional; enforces plan workspace limit
curl -sS -X POST "$BASE_URL/api/workspaces" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"slug":"","name":"My Business","businessType":"RESTAURANT","googleReviewLink":"https://maps.google.com/?cid=example","phone":"+15555550101"}'

### Get workspace by id
curl -sS "$BASE_URL/api/workspaces/$WORKSPACE_ID" "${hdr_auth[@]}"

### Update workspace — merge partial fields as supported by API
curl -sS -X PUT "$BASE_URL/api/workspaces/$WORKSPACE_ID" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"slug":"my-brand","name":"Renamed Co","businessType":"SALON","googleReviewLink":"https://maps.google.com/?cid=x","phone":"+15555550102"}'

### Logo metadata — hosted URL string only
curl -sS -X POST "$BASE_URL/api/workspaces/$WORKSPACE_ID/logo-metadata" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"logoUrl":"https://cdn.example.com/logo.png"}'

### Patch theme — partial JSON
curl -sS -X PATCH "$BASE_URL/api/workspaces/$WORKSPACE_ID/theme" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"primaryColor":"#6366f1","secondaryColor":"#64748b","accentColor":"#0ea5e9","darkModeEnabled":false,"logoPosition":"TOP_CENTER","buttonStyle":"ROUNDED"}'

### Analytics — query days (default 30)
curl -sS "$BASE_URL/api/workspaces/$WORKSPACE_ID/analytics?days=30" "${hdr_auth[@]}"

### QR PNG — binary response (save with -o qr.png)
curl -sS "$BASE_URL/api/workspaces/$WORKSPACE_ID/qr.png?size=640" "${hdr_auth[@]}" -o qr.png

### WhatsApp deep link JSON
curl -sS "$BASE_URL/api/workspaces/$WORKSPACE_ID/whatsapp-link" "${hdr_auth[@]}"

# -----------------------------------------------------------------------------
# ADMIN — JWT with role ADMIN
# -----------------------------------------------------------------------------

PLAN_ID="${PLAN_ID:-1}"
LABEL_ID="${LABEL_ID:-1}"
TEMPLATE_ID="${TEMPLATE_ID:-1}"

### Admin — list all subscription plans
curl -sS "$BASE_URL/api/admin/subscription-plans" "${hdr_auth[@]}"

### Admin — create plan
curl -sS -X POST "$BASE_URL/api/admin/subscription-plans" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"name":"Pro","durationType":"MONTHLY","maxWorkspaces":10,"price":99.00,"featuresJson":"{\"tier\":\"pro\"}","active":true}'

### Admin — replace plan
curl -sS -X PUT "$BASE_URL/api/admin/subscription-plans/$PLAN_ID" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"name":"Pro","durationType":"MONTHLY","maxWorkspaces":12,"price":89.00,"featuresJson":"{\"tier\":\"pro\"}","active":true}'

### Admin — toggle plan active
curl -sS -X PATCH "$BASE_URL/api/admin/subscription-plans/$PLAN_ID/active" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"active":false}'

### Admin — list dynamic labels
curl -sS "$BASE_URL/api/admin/dynamic-labels" "${hdr_auth[@]}"

### Admin — create dynamic label (workspaceId null = global)
curl -sS -X POST "$BASE_URL/api/admin/dynamic-labels" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"workspaceId":null,"labelKey":"cta_primary_review","labelValue":"Tell us how we did","category":"cta","locale":"en"}'

### Admin — replace dynamic label
curl -sS -X PUT "$BASE_URL/api/admin/dynamic-labels/$LABEL_ID" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"workspaceId":null,"labelKey":"cta_primary_review","labelValue":"Share your experience","category":"cta","locale":"en"}'

### Admin — delete dynamic label
curl -sS -X DELETE "$BASE_URL/api/admin/dynamic-labels/$LABEL_ID" "${hdr_auth[@]}"

### Admin — list WhatsApp templates
curl -sS "$BASE_URL/api/admin/whatsapp-templates" "${hdr_auth[@]}"

### Admin — create WhatsApp template
curl -sS -X POST "$BASE_URL/api/admin/whatsapp-templates" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"templateKey":"review_invite","workspaceId":null,"messageTemplate":"Hi {{business_name}} — {{review_link}}","active":true}'

### Admin — replace WhatsApp template
curl -sS -X PUT "$BASE_URL/api/admin/whatsapp-templates/$TEMPLATE_ID" \
  -H "Content-Type: application/json" "${hdr_auth[@]}" \
  -d '{"templateKey":"review_invite","workspaceId":null,"messageTemplate":"Updated {{public_landing_url}}","active":true}'
