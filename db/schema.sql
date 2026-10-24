-- 점검/장애 접수 게시판 스키마 (DB 서버에서 관리자(root)로 실행)
--   sudo mysql < schema.sql
-- 앱 계정 생성과 권한 부여는 가이드의 Part 2 에서 따로 합니다.

CREATE DATABASE IF NOT EXISTS boarddb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE boarddb;

CREATE TABLE IF NOT EXISTS ticket (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  title       VARCHAR(200) NOT NULL,
  server_name VARCHAR(100) NOT NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
  content     TEXT,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ticket_status (status),
  KEY idx_ticket_created (created_at)
) ENGINE=InnoDB;

INSERT INTO ticket (title, server_name, status, content) VALUES
  ('디스크 사용률 85% 경고', 'was01', 'OPEN',    '/opt/tomcat/logs 용량 증가. 로그 로테이션 설정 확인 필요.'),
  ('월간 정기 패치 적용',     'web01', 'WORKING', 'dnf upgrade 후 재부팅 예정.'),
  ('DB 슬로우 쿼리 점검',     'db01',  'DONE',    'long_query_time=1 설정 후 슬로우 로그 확인 완료.');
