package com.back.global.web;

import com.back.global.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // 모든 컨트롤러에 공통으로 적용된다.
@RequiredArgsConstructor
public class GlobalModelAttribute {
    private final CommonUtil commonUtil;

    // 모든 컨트롤러의 액션 메서드가 실행되기 전에 실행되어, 모델에 "commonUtil" 을 넣어 준다.
    // → 모든 템플릿에서 ${commonUtil.markdown(...)} 으로 쓸 수 있다.
    @ModelAttribute("commonUtil")
    public CommonUtil commonUtil() {
        return commonUtil;
    }
}
