#!/usr/bin/env bash
# board 앱 배포/롤백/drain 스크립트 — WAS 서버에서 root 권한(sudo)으로 실행합니다.
#
#   deploy.sh deploy <새 WAR 경로> <라벨(예: 빌드번호)>   백업 → 교체 → 헬스체크, 실패하면 자동 롤백
#   deploy.sh rollback [백업 WAR 경로]                    생략하면 가장 최근 백업으로 되돌림
#   deploy.sh drain | undrain                             로드밸런서에서 이 서버 빼기 / 넣기
#   deploy.sh status                                      상태 확인
#
# Jenkins 가 하는 일도, 사람이 손으로 하는 일도 이 스크립트 하나로 같게 만들어 두는 것이 핵심입니다.
set -euo pipefail

TOMCAT_HOME="${TOMCAT_HOME:-/opt/tomcat}"
SERVICE="${SERVICE:-tomcat}"
WEBAPPS="$TOMCAT_HOME/webapps"
BACKUP_DIR="${BACKUP_DIR:-/opt/deploy/backup}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/health}"
MAINT_FILE="${MAINT_FILE:-/etc/boardapp/maintenance}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-90}"   # 헬스체크를 기다리는 최대 시간(초)
KEEP="${KEEP:-5}"                        # 보관할 백업 개수

log() { echo "[$(date '+%F %T')] $*"; }

usage() {
  sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'
}

wait_healthy() {
  local deadline=$(( $(date +%s) + HEALTH_TIMEOUT ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    if curl -fsS -m 3 -o /dev/null "$HEALTH_URL"; then
      return 0
    fi
    sleep 3
  done
  return 1
}

# Tomcat 을 멈추고 ROOT.war 를 교체한 뒤 다시 시작
install_war() {
  local war="$1"
  systemctl stop "$SERVICE"
  rm -rf "$WEBAPPS/ROOT" "$WEBAPPS/ROOT.war"
  cp "$war" "$WEBAPPS/ROOT.war"
  chown tomcat:tomcat "$WEBAPPS/ROOT.war"
  systemctl start "$SERVICE"
}

backup_current() {
  local label="$1"
  mkdir -p "$BACKUP_DIR"
  if [ -f "$WEBAPPS/ROOT.war" ]; then
    local dst
    dst="$BACKUP_DIR/ROOT-$(date +%Y%m%d-%H%M%S)-before-${label}.war"
    cp -p "$WEBAPPS/ROOT.war" "$dst"
    log "백업: $dst"
  else
    log "현재 배포된 WAR 가 없어 백업을 건너뜁니다 (최초 배포)"
  fi
}

latest_backup() {
  ls -1t "$BACKUP_DIR"/ROOT-*.war 2>/dev/null | head -n 1 || true
}

prune_backups() {
  ls -1t "$BACKUP_DIR"/ROOT-*.war 2>/dev/null | tail -n +"$((KEEP + 1))" | xargs -r rm -f || true
}

do_rollback() {
  local target="${1:-$(latest_backup)}"
  if [ -z "$target" ] || [ ! -f "$target" ]; then
    log "롤백할 백업이 없습니다."
    return 2
  fi
  log "롤백 대상: $target"
  install_war "$target"
  if wait_healthy; then
    log "롤백 성공 (이전 버전으로 복구됨)"
    return 0
  fi
  log "롤백 후에도 헬스체크 실패 → 사람이 직접 확인해야 합니다 (journalctl -u $SERVICE, $TOMCAT_HOME/logs/catalina.out)"
  return 2
}

do_deploy() {
  local war="${1:-}" label="${2:-manual}"
  if [ -z "$war" ] || [ ! -f "$war" ]; then
    log "WAR 파일이 없습니다: '$war'"
    exit 1
  fi
  backup_current "$label"
  log "배포 시작: $war (label=$label)"
  install_war "$war"
  if wait_healthy; then
    log "헬스체크 통과 → 배포 성공"
    prune_backups
    return 0
  fi
  log "헬스체크 실패 → 자동 롤백을 시도합니다"
  do_rollback "$(latest_backup)" || true
  exit 1   # Jenkins 빌드는 '실패'로 표시되어야 하므로 롤백에 성공해도 실패 코드로 종료
}

do_drain() {
  mkdir -p "$(dirname "$MAINT_FILE")"
  touch "$MAINT_FILE"
  log "DRAIN: /ready 가 503 을 돌려주므로 로드밸런서가 이 서버로 새 요청을 보내지 않게 됩니다"
}

do_undrain() {
  rm -f "$MAINT_FILE"
  log "UNDRAIN: 로드밸런서가 헬스체크를 통과하면 이 서버로 다시 요청을 보냅니다"
}

do_status() {
  echo "tomcat: $(systemctl is-active "$SERVICE" || true)"
  echo "health: $(curl -sS -m 3 "$HEALTH_URL" || echo '응답 없음')"
  if [ -f "$MAINT_FILE" ]; then echo "drain : ON"; else echo "drain : off"; fi
  echo "backups:"
  ls -lt "$BACKUP_DIR" 2>/dev/null | head -n 6 || true
}

cmd="${1:-}"
shift || true
case "$cmd" in
  deploy)   do_deploy "$@" ;;
  rollback) do_rollback "${1:-}" ;;
  drain)    do_drain ;;
  undrain)  do_undrain ;;
  status)   do_status ;;
  *)        usage; exit 1 ;;
esac
