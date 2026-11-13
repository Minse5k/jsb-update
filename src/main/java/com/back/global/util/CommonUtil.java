package com.back.global.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

@Component // 빈으로 등록 → GlobalModelAttribute 가 모든 템플릿에 commonUtil 로 넘겨준다.
public class CommonUtil {
    // 마크다운 문법으로 쓴 글을 HTML 로 바꾼다. 예) **굵게** → <strong>굵게</strong>
    public String markdown(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown); // 마크다운 → 문서 구조(트리)

        HtmlRenderer renderer = HtmlRenderer
                .builder()
                .escapeHtml(true) // 글에 직접 쓴 HTML 태그(<script> 등)는 실행되지 않게 &lt;script&gt; 글자로 바꾼다.
                .sanitizeUrls(true) // [링크](javascript:...) 같은 위험한 주소를 막는다.
                .build();
        return renderer.render(document); // 문서 구조 → HTML 문자열
    }
}
