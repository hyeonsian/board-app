package com.example.board;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 점검/장애 접수 한 건. 폼 바인딩을 위해 getter/setter 를 가진 일반 클래스로 둡니다. */
public class Ticket {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String title;
    private String serverName;
    private String status = "OPEN";
    private String content;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 화면에 보여줄 상태 이름 (DB 에는 영문 코드로 저장) */
    public String getStatusLabel() {
        String s = status == null ? "" : status;
        return switch (s) {
            case "OPEN" -> "접수";
            case "WORKING" -> "처리중";
            case "DONE" -> "완료";
            default -> s;
        };
    }

    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.format(FMT);
    }
}
