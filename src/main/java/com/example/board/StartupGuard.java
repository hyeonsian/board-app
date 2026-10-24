package com.example.board;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 롤백 실습용 장치. app.fail-on-start=true 로 빌드/배포하면 앱이 일부러 기동에 실패합니다.
 * (실제 장애: 잘못된 설정값, 없는 파일, 맞지 않는 라이브러리 때문에 기동 실패하는 상황의 연습)
 */
@Component
public class StartupGuard {

    public StartupGuard(@Value("${app.fail-on-start:false}") boolean failOnStart) {
        if (failOnStart) {
            throw new IllegalStateException(
                    "app.fail-on-start=true 이므로 일부러 기동을 실패시킵니다 (롤백 실습용)");
        }
    }
}
