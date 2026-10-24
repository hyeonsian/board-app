package com.example.board;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** 모든 화면의 하단(footer)에 버전/서버 정보를 보여주기 위해 모델에 공통으로 넣습니다. */
@ControllerAdvice
public class GlobalModelAttributes {

    private final AppInfo info;

    public GlobalModelAttributes(AppInfo info) {
        this.info = info;
    }

    @ModelAttribute("app")
    public AppInfo app() {
        return info;
    }
}
