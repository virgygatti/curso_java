#!/usr/bin/env bash
# Suite amplia de pruebas HTTP (curl). Git Bash / WSL / Linux / macOS.
# Uso: chmod +x scripts/test-api.sh && ./scripts/test-api.sh
# Opcional: API_BASE_URL=http://127.0.0.1:8080 ./scripts/test-api.sh
#
# Cubre: listados, GET/POST/PUT/DELETE, 200 vs 409, validaciones, FKs, líneas, JSON inválido.
# Requisitos: curl; app Spring en marcha. Python 3 opcional (extrae id de línea si el grep falla).

set -euo pipefail

BASE="${API_BASE_URL:-http://localhost:8080}"
BASE="${BASE%/}"

RED='\033[0;31m'
GRN='\033[0;32m'
CYA='\033[0;36m'
YLW='\033[0;33m'
RST='\033[0m'

ok()   { echo -e "${GRN}[OK]${RST} $*"; }
skip() { echo -e "${YLW}[SKIP]${RST} $*"; }
fail() { echo -e "${RED}[FAIL]${RST} $*" >&2; exit 1; }

tmp_resp() {
  if command -v mktemp >/dev/null 2>&1; then
    mktemp 2>/dev/null || mktemp -t testapi 2>/dev/null || echo "${TMPDIR:-/tmp}/test-api-$$.json"
  else
    echo "${TMPDIR:-/tmp}/test-api-$$.json"
  fi
}

read_first_id() {
  local f="$1"
  grep -oE '"id"[[:space:]]*:[[:space:]]*[0-9]+' "$f" 2>/dev/null | head -1 | grep -oE '[0-9]+' || true
}

read_comprobante_id() {
  local f="$1"
  grep -o '"comprobanteId"[[:space:]]*:[[:space:]]*[0-9]*' "$f" 2>/dev/null | head -1 | grep -oE '[0-9]+' || true
}

# Primer id dentro de lineas[] del JSON de GET comprobante.
read_linea_id_from_comprobante_json() {
  local f="$1"
  local id=""
  if command -v python3 >/dev/null 2>&1; then
    id=$(python3 -c "import json; d=json.load(open('$f')); print(d['lineas'][0]['id'])" 2>/dev/null) || true
    [[ -n "$id" ]] && { echo "$id"; return; }
  fi
  if command -v python >/dev/null 2>&1; then
    id=$(python -c "import json; d=json.load(open('$f')); print(d['lineas'][0]['id'])" 2>/dev/null) || true
    [[ -n "$id" ]] && { echo "$id"; return; }
  fi
  grep -oE '"lineas"[[:space:]]*:[[:space:]]*\[[[:space:]]*\{[[:space:]]*"id"[[:space:]]*:[[:space:]]*[0-9]+' "$f" 2>/dev/null \
    | head -1 | grep -oE '[0-9]+$' || true
}

dump_body() {
  local f="$1"
  if [[ -f "$f" ]]; then
    echo "--- cuerpo respuesta ---" >&2
    cat "$f" >&2
    echo "" >&2
    echo "--- fin ---" >&2
  fi
}

fail_http() {
  local msg="$1"
  local file="${2:-}"
  echo -e "${RED}[FAIL]${RST} $msg" >&2
  [[ -n "$file" ]] && dump_body "$file"
  exit 1
}

http_req() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local tmp
  tmp="$(tmp_resp)"
  local code
  if [[ -n "$body" ]]; then
    code="$(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json; charset=utf-8" \
      --data-binary "$body")"
  else
    code="$(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" "$url")"
  fi
  [[ "$code" =~ ^[0-9]{3}$ ]] || fail_http "curl inválido (¿app arriba en $BASE?): código='$code'" "$tmp"
  echo "$code|$tmp"
}

expect_code() {
  local actual="$1"
  shift
  local exp
  for exp in "$@"; do
    [[ "$actual" == "$exp" ]] && return 0
  done
  return 1
}

expect_409_errores() {
  local file="$1"
  local ctx="$2"
  grep -q '"errores"' "$file" || fail_http "$ctx: falta clave errores" "$file"
}

echo -e "\n${CYA}=== Facturación API — suite ampliada ===${RST}"
echo "Base URL: $BASE"
echo ""

echo "--- Listados GET (200) ---"
for path in "/api/clientes" "/api/productos" "/api/comprobantes" "/api/lineas-comprobante"; do
  out="$(http_req GET "$BASE$path")"
  code="${out%%|*}"
  file="${out#*|}"
  expect_code "$code" 200 || fail_http "GET $path -> $code" "$file"
  rm -f "$file"
  ok "GET $path -> 200"
done

echo ""
echo "--- GET recurso inexistente (409 + errores) ---"
for path in "/api/clientes/999999999" "/api/productos/999999999" "/api/comprobantes/999999999" "/api/lineas-comprobante/999999999"; do
  out="$(http_req GET "$BASE$path")"
  code="${out%%|*}"
  file="${out#*|}"
  expect_code "$code" 409 || fail_http "GET $path -> esperaba 409, fue $code" "$file"
  expect_409_errores "$file" "GET $path"
  rm -f "$file"
  ok "GET $path -> 409"
done

echo ""
echo "--- Alta cliente + producto (200) ---"
SUFFIX="$(date +%s)-$$-$RANDOM"
DOC="$(printf 'T%s%04d' "$(date +%s)" "$((RANDOM % 10000))")"
[[ "${#DOC}" -le 20 ]] || DOC="${DOC:0:20}"

BODY_CLIENT=$(printf '{"nombre":"Test","apellido":"Automatico","documento":"%s"}' "$DOC")
out="$(http_req POST "$BASE/api/clientes" "$BODY_CLIENT")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "POST /api/clientes" "$file"
CLIENTE_ID="$(read_first_id "$file")"
rm -f "$file"
[[ -n "${CLIENTE_ID:-}" ]] || fail "POST cliente sin id"

ok "POST /api/clientes (id=$CLIENTE_ID)"

echo ""
echo "--- Unicidad documento (409, mismo POST repetido) ---"
out="$(http_req POST "$BASE/api/clientes" "$BODY_CLIENT")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST cliente documento duplicado" "$file"
expect_409_errores "$file" "documento duplicado"
rm -f "$file"
ok "POST /api/clientes (documento repetido) -> 409"

CODIGO="AUTO-SKU-${SUFFIX}"
[[ "${#CODIGO}" -le 50 ]] || CODIGO="${CODIGO:0:50}"
BODY_PROD=$(printf '{"codigo":"%s","nombre":"Producto test","descripcion":"Script","precio":100.50,"stock":50}' "$CODIGO")
out="$(http_req POST "$BASE/api/productos" "$BODY_PROD")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "POST /api/productos" "$file"
PRODUCTO_ID="$(read_first_id "$file")"
rm -f "$file"
[[ -n "${PRODUCTO_ID:-}" ]] || fail "POST producto sin id"

ok "POST /api/productos (id=$PRODUCTO_ID)"

echo ""
echo "--- Unicidad codigo producto (409) ---"
out="$(http_req POST "$BASE/api/productos" "$BODY_PROD")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST producto codigo duplicado" "$file"
expect_409_errores "$file" "codigo duplicado"
rm -f "$file"
ok "POST /api/productos (codigo repetido) -> 409"

CODIGO2="X2-${SUFFIX}"
[[ "${#CODIGO2}" -le 50 ]] || CODIGO2="${CODIGO2:0:50}"
BODY_PROD2=$(printf '{"codigo":"%s","nombre":"Segundo","descripcion":"x","precio":25.00,"stock":100}' "$CODIGO2")
out="$(http_req POST "$BASE/api/productos" "$BODY_PROD2")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "POST segundo producto" "$file"
PRODUCTO2_ID="$(read_first_id "$file")"
rm -f "$file"
[[ -n "${PRODUCTO2_ID:-}" ]] || fail "POST producto 2 sin id"
ok "POST /api/productos segundo (id=$PRODUCTO2_ID)"

echo ""
echo "--- Validación POST cliente/producto (409) ---"
DOC_BAD="$(printf 'X%s' "$(date +%s)")"
[[ "${#DOC_BAD}" -le 20 ]] || DOC_BAD="${DOC_BAD:0:20}"
out="$(http_req POST "$BASE/api/clientes" "$(printf '{"nombre":"","apellido":"X","documento":"%s"}' "$DOC_BAD")")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST cliente nombre vacío" "$file"
expect_409_errores "$file" "POST cliente inválido"
rm -f "$file"
ok "POST /api/clientes (nombre vacío) -> 409"

COD_BAD="Z$(date +%s)"
out="$(http_req POST "$BASE/api/productos" "$(printf '{"codigo":"%s","nombre":"","precio":10,"stock":1}' "$COD_BAD")")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST producto nombre vacío" "$file"
expect_409_errores "$file" "POST producto inválido"
rm -f "$file"
ok "POST /api/productos (nombre vacío) -> 409"

echo ""
echo "--- PUT id inexistente (409) ---"
out="$(http_req PUT "$BASE/api/clientes/999999999" "$BODY_CLIENT")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "PUT cliente inexistente" "$file"
expect_409_errores "$file" "PUT cliente 999999999"
rm -f "$file"
ok "PUT /api/clientes/999999999 -> 409"

out="$(http_req PUT "$BASE/api/productos/999999999" "$BODY_PROD")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "PUT producto inexistente" "$file"
expect_409_errores "$file" "PUT producto 999999999"
rm -f "$file"
ok "PUT /api/productos/999999999 -> 409"

echo ""
echo "--- POST comprobante: JSON inválido / vacíos / FK (409) ---"
out="$(http_req POST "$BASE/api/comprobantes" '{ no json')"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST JSON basura" "$file"
expect_409_errores "$file" "JSON inválido"
rm -f "$file"
ok "POST /api/comprobantes (JSON inválido) -> 409"

EMPTY_LINES=$(printf '{"cliente":{"clienteid":%s},"lineas":[]}' "$CLIENTE_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$EMPTY_LINES")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST lineas vacías" "$file"
expect_409_errores "$file" "lineas vacías"
rm -f "$file"
ok "POST /api/comprobantes (lineas []) -> 409"

NO_CLIENT=$(printf '{"lineas":[{"cantidad":1,"producto":{"productoid":%s}}]}' "$PRODUCTO_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$NO_CLIENT")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST sin cliente" "$file"
expect_409_errores "$file" "sin cliente"
rm -f "$file"
ok "POST /api/comprobantes (sin cliente) -> 409"

CANT_ZERO=$(printf '{"cliente":{"clienteid":%s},"lineas":[{"cantidad":0,"producto":{"productoid":%s}}]}' "$CLIENTE_ID" "$PRODUCTO_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$CANT_ZERO")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST cantidad 0" "$file"
expect_409_errores "$file" "cantidad 0"
rm -f "$file"
ok "POST /api/comprobantes (cantidad 0) -> 409"

BAD_CLIENT_COMP=$(printf '{"cliente":{"clienteid":999999999},"lineas":[{"cantidad":1,"producto":{"productoid":%s}}]}' "$PRODUCTO_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$BAD_CLIENT_COMP")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST cliente id inválido" "$file"
expect_409_errores "$file" "cliente inválido"
rm -f "$file"
ok "POST /api/comprobantes (cliente inexistente) -> 409"

BAD_PROD_COMP=$(printf '{"cliente":{"clienteid":%s},"lineas":[{"cantidad":1,"producto":{"productoid":999999999}}]}' "$CLIENTE_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$BAD_PROD_COMP")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST producto id inválido" "$file"
expect_409_errores "$file" "producto inválido"
rm -f "$file"
ok "POST /api/comprobantes (producto inexistente) -> 409"

echo ""
echo "--- POST comprobante OK + multiproducto (200) ---"
BODY_COMP=$(printf '{"cliente":{"clienteid":%s},"lineas":[{"cantidad":2,"producto":{"productoid":%s}},{"cantidad":1,"producto":{"productoid":%s}}]}' \
  "$CLIENTE_ID" "$PRODUCTO_ID" "$PRODUCTO2_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$BODY_COMP")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "POST comprobante multiproducto" "$file"
COMP_ID="$(read_comprobante_id "$file")"
TOTAL_HINT=$(grep -o '"total"[[:space:]]*:[[:space:]]*[0-9.]*' "$file" | grep -oE '[0-9.]+$' | head -1 || true)
rm -f "$file"
[[ -n "${COMP_ID:-}" ]] || fail "sin comprobanteId"
ok "POST /api/comprobantes multiproducto -> 200 (comprobanteId=$COMP_ID total≈$TOTAL_HINT)"

out="$(http_req GET "$BASE/api/comprobantes/$COMP_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "GET comprobante" "$file"
LINEA_ID="$(read_linea_id_from_comprobante_json "$file")"
rm -f "$file"
ok "GET /api/comprobantes/$COMP_ID -> 200"
if [[ -z "${LINEA_ID:-}" ]]; then
  skip "No se pudo obtener linea id (instalá python3 para parseo fiable)"
else
  ok "Primera línea id=$LINEA_ID"
fi

echo ""
echo "--- FK: DELETE producto/cliente con venta pendiente (409) ---"
out="$(http_req DELETE "$BASE/api/productos/$PRODUCTO_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "DELETE producto usado en línea debería 409" "$file"
expect_409_errores "$file" "DELETE producto con FK"
rm -f "$file"
ok "DELETE /api/productos/$PRODUCTO_ID (referenciado) -> 409"

out="$(http_req DELETE "$BASE/api/clientes/$CLIENTE_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "DELETE cliente con comprobante debería 409" "$file"
expect_409_errores "$file" "DELETE cliente con FK"
rm -f "$file"
ok "DELETE /api/clientes/$CLIENTE_ID (tiene comprobantes) -> 409"

echo ""
echo "--- POST comprobante sin stock (409) ---"
BODY_STOCK=$(printf '{"cliente":{"clienteid":%s},"lineas":[{"cantidad":999999,"producto":{"productoid":%s}}]}' "$CLIENTE_ID" "$PRODUCTO2_ID")
out="$(http_req POST "$BASE/api/comprobantes" "$BODY_STOCK")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "POST sin stock" "$file"
expect_409_errores "$file" "stock"
grep -qi stock "$file" || fail_http "mensaje sin stock" "$file"
rm -f "$file"
ok "POST /api/comprobantes (stock insuficiente) -> 409"

echo ""
echo "--- PUT cliente y producto (200) ---"
BODY_PUT=$(printf '{"nombre":"Actualizado","apellido":"Automatico","documento":"%s"}' "$DOC")
out="$(http_req PUT "$BASE/api/clientes/$CLIENTE_ID" "$BODY_PUT")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "PUT cliente" "$file"
rm -f "$file"
ok "PUT /api/clientes/$CLIENTE_ID -> 200"

BODY_PUT_P=$(printf '{"codigo":"%s","nombre":"ProdAct","descripcion":"y","precio":110.00,"stock":40}' "$CODIGO")
out="$(http_req PUT "$BASE/api/productos/$PRODUCTO_ID" "$BODY_PUT_P")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "PUT producto" "$file"
rm -f "$file"
ok "PUT /api/productos/$PRODUCTO_ID -> 200"

echo ""
if [[ -n "${LINEA_ID:-}" ]]; then
  echo "--- GET línea por id (200) ---"
  out="$(http_req GET "$BASE/api/lineas-comprobante/$LINEA_ID")"
  code="${out%%|*}"
  file="${out#*|}"
  expect_code "$code" 200 || fail_http "GET línea" "$file"
  rm -f "$file"
  ok "GET /api/lineas-comprobante/$LINEA_ID -> 200"
else
  skip "GET línea por id (sin LINEA_ID; instalá python3 para parsear JSON)"
fi

echo ""
echo "--- DELETE comprobante (200); luego DELETE producto/cliente (200) ---"
out="$(http_req DELETE "$BASE/api/comprobantes/$COMP_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "DELETE comprobante" "$file"
rm -f "$file"
ok "DELETE /api/comprobantes/$COMP_ID -> 200"

out="$(http_req GET "$BASE/api/comprobantes/$COMP_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 409 || fail_http "GET comprobante borrado debería 409" "$file"
rm -f "$file"
ok "GET /api/comprobantes/$COMP_ID (borrado) -> 409"

out="$(http_req DELETE "$BASE/api/productos/$PRODUCTO_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "DELETE producto 1" "$file"
rm -f "$file"
ok "DELETE /api/productos/$PRODUCTO_ID -> 200"

out="$(http_req DELETE "$BASE/api/productos/$PRODUCTO2_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "DELETE producto 2" "$file"
rm -f "$file"
ok "DELETE /api/productos/$PRODUCTO2_ID -> 200"

out="$(http_req DELETE "$BASE/api/clientes/$CLIENTE_ID")"
code="${out%%|*}"
file="${out#*|}"
expect_code "$code" 200 || fail_http "DELETE cliente" "$file"
rm -f "$file"
ok "DELETE /api/clientes/$CLIENTE_ID -> 200"

echo ""
echo -e "${GRN}Todos los pasos pasaron.${RST}"
