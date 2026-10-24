# board-app — 서버 점검/장애 접수 게시판 (CI/CD 실습용)

Apache → Tomcat → MariaDB 3-tier 와 Jenkins CI/CD 를 손으로 익히기 위한 샘플 앱입니다.
앱 코드가 목적이 아니므로 코드는 읽기만 하고, `pom.xml` / `application.properties` / WAR 구조를 이해하는 데 집중하세요.

## 구조

| 경로 | 설명 |
| --- | --- |
| `pom.xml` | Maven 빌드 정의 (WAR 패키징, Spring Boot 3.5, JDK 21) |
| `src/main/java/.../` | 컨트롤러, JDBC 저장소, 헬스체크 |
| `src/main/resources/application.properties` | 기본 설정. 운영 비밀값은 서버의 `/etc/boardapp/application.properties` 로 분리 |
| `src/main/resources/templates/` | Thymeleaf 화면 |
| `db/schema.sql` | DB 스키마와 샘플 데이터 |
| `deploy/deploy.sh` | WAS 서버에서 실행하는 배포/롤백/drain 스크립트 |
| `Jenkinsfile` | Part 8 파이프라인 |
| `Jenkinsfile.part10` | Part 10 파이프라인 (승인 + 롤링 배포). Part 10 에서 `Jenkinsfile` 에 덮어씀 |

## 엔드포인트

| 경로 | 용도 |
| --- | --- |
| `/` , `/tickets` | 접수 목록, 검색 |
| `/tickets/new`, `/tickets/{id}` | 등록, 상세/수정/삭제 |
| `/health` | 앱과 DB 상태 (JSON, 버전/빌드번호/호스트명 포함). DB 장애 시 503 |
| `/ready` | 로드밸런서용. drain 중이거나 DB 장애면 503 |
| `/slow?sec=40` | 일부러 느린 응답 (Apache-Tomcat 타임아웃 재현) |

## 로컬 빌드

```bash
mvn -B clean package -Dbuild.number=1
ls -lh target/ROOT.war
```

## 주의

- Spring Boot 3.5 의 무료 지원은 2026-06-30 에 종료됐습니다. 실습용으로만 쓰고 운영에 쓰지 마세요.
- 저장소에 비밀번호, 키, 토큰을 커밋하지 마세요.

