package com.example.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 단위 테스트: DB, 서버 없이 빠르게 도는 테스트. Jenkins 의 "Build & Test" 단계에서 실행됩니다.
 * (실습: 아래 기대값을 일부러 틀리게 고쳐 push 하면 테스트에서 빌드가 멈추고 배포까지 가지 않습니다.)
 */
class TicketTest {

    @Test
    void statusLabelMapsKnownCodes() {
        Ticket t = new Ticket();
        t.setStatus("OPEN");
        assertThat(t.getStatusLabel()).isEqualTo("접수");
        t.setStatus("WORKING");
        assertThat(t.getStatusLabel()).isEqualTo("처리중");
        t.setStatus("DONE");
        assertThat(t.getStatusLabel()).isEqualTo("완료");
    }

    @Test
    void unknownStatusFallsBackToCode() {
        Ticket t = new Ticket();
        t.setStatus("ARCHIVED");
        assertThat(t.getStatusLabel()).isEqualTo("ARCHIVED");
    }

    @Test
    void newTicketStartsAsOpen() {
        assertThat(new Ticket().getStatus()).isEqualTo("OPEN");
    }
}
