package com.example.board;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영 점검용 엔드포인트
 *  - /health : 앱과 DB 가 살아 있는가 (배포 스크립트, Jenkins 스모크 테스트가 사용)
 *  - /ready  : 트래픽을 받아도 되는가 (Apache 로드밸런서 헬스체크가 사용, drain 중이면 503)
 *  - /slow   : 일부러 느린 응답 (Apache-Tomcat 타임아웃 502/504 재현용)
 */
@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbc;
    private final AppInfo info;

    public HealthController(JdbcTemplate jdbc, AppInfo info) {
        this.jdbc = jdbc;
        this.info = info;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbUp = dbUp();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", dbUp ? "UP" : "DOWN");
        body.put("db", dbUp ? "UP" : "DOWN");
        body.putAll(info.toMap());
        return ResponseEntity.status(dbUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        if (info.isDraining()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "DRAINING");
            body.putAll(info.toMap());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
        return health();
    }

    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "10") int sec) throws InterruptedException {
        int s = Math.min(Math.max(sec, 0), 300);
        Thread.sleep(s * 1000L);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sleptSeconds", s);
        body.putAll(info.toMap());
        return body;
    }

    private boolean dbUp() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            return one != null && one == 1;
        } catch (Exception e) {
            log.warn("DB 헬스체크 실패: {}", e.toString());
            return false;
        }
    }
}
