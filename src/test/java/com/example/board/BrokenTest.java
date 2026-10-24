package com.example.board;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BrokenTest {
    @Test
    void alwaysFails() {
        assertEquals(1, 2, "일부러 실패시키는 테스트");
    }
}
