package com.example.board;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JdbcTemplate 으로 SQL 을 직접 실행합니다. (ORM 의 마법 없이 "어떤 SQL 이 DB 로 가는지" 눈에 보이게 하려는 선택)
 * DB 접속 풀(HikariCP)은 Spring Boot 가 자동 구성하며, 설정은 application.properties 의
 * spring.datasource.* 에 있습니다.
 */
@Repository
public class TicketRepository {

    private static final String COLS = "id, title, server_name, status, content, created_at";

    private static final RowMapper<Ticket> MAPPER = (rs, rowNum) -> {
        Ticket t = new Ticket();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        t.setServerName(rs.getString("server_name"));
        t.setStatus(rs.getString("status"));
        t.setContent(rs.getString("content"));
        Timestamp ts = rs.getTimestamp("created_at");
        t.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return t;
    };

    private final JdbcTemplate jdbc;

    public TicketRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Ticket> findAll(String q) {
        if (q == null || q.isBlank()) {
            return jdbc.query("SELECT " + COLS + " FROM ticket ORDER BY id DESC LIMIT 200", MAPPER);
        }
        String like = "%" + q.trim() + "%";
        return jdbc.query(
                "SELECT " + COLS + " FROM ticket WHERE title LIKE ? OR server_name LIKE ? ORDER BY id DESC LIMIT 200",
                MAPPER, like, like);
    }

    public Optional<Ticket> findById(long id) {
        List<Ticket> list = jdbc.query("SELECT " + COLS + " FROM ticket WHERE id = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    public void insert(Ticket t) {
        jdbc.update("INSERT INTO ticket (title, server_name, status, content) VALUES (?, ?, ?, ?)",
                t.getTitle(), t.getServerName(), t.getStatus(), t.getContent());
    }

    public int update(long id, Ticket t) {
        return jdbc.update("UPDATE ticket SET title = ?, server_name = ?, status = ?, content = ? WHERE id = ?",
                t.getTitle(), t.getServerName(), t.getStatus(), t.getContent(), id);
    }

    public int delete(long id) {
        return jdbc.update("DELETE FROM ticket WHERE id = ?", id);
    }
}
