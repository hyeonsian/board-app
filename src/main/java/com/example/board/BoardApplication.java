package com.example.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 외부 Tomcat(WAR 배포)에서 기동하려면 SpringBootServletInitializer 를 상속해야 합니다.
 * Tomcat 이 이 클래스를 찾아 애플리케이션을 띄웁니다. main() 은 IDE 에서 직접 실행할 때만 쓰입니다.
 */
@SpringBootApplication
public class BoardApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BoardApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }
}
