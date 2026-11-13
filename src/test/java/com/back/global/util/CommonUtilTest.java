package com.back.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonUtilTest {
    private final CommonUtil commonUtil = new CommonUtil(); // 스프링 없이 순수 자바로 테스트

    @Test
    @DisplayName("markdown : 마크다운 문법이 HTML 로 바뀐다.")
    void t1() {
        assertThat(commonUtil.markdown("**굵게**")).isEqualTo("<p><strong>굵게</strong></p>\n");
        assertThat(commonUtil.markdown("- 하나\n- 둘")).contains("<ul>", "<li>하나</li>", "<li>둘</li>");
        assertThat(commonUtil.markdown("```\ncode\n```")).contains("<pre><code>code");
    }

    @Test
    @DisplayName("markdown : 글에 직접 쓴 HTML 태그와 javascript: 링크는 실행되지 않게 바뀐다.(XSS 방지)")
    void t2() {
        String html = commonUtil.markdown("<script>alert(1)</script>");
        assertThat(html).doesNotContain("<script>");
        assertThat(html).contains("&lt;script&gt;");

        assertThat(commonUtil.markdown("[클릭](javascript:alert(1))")).doesNotContain("javascript:");
    }
}
