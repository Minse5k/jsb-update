// data-markdown-editor 가 붙은 textarea 를 마크다운 편집기(EasyMDE)로 바꾼다.
// 편집기는 textarea 를 숨기고 그 자리에 편집 화면을 그린다. 폼을 보낼 때는 편집기 내용이 textarea 로 옮겨져서
// 서버는 지금처럼 content 파라미터를 그대로 받는다. (서버 코드는 바뀌지 않는다)
document.addEventListener("DOMContentLoaded", () => {
    if (typeof EasyMDE === "undefined") return; // CDN 을 못 불러오면 그냥 textarea 로 쓴다.

    document.querySelectorAll("textarea[data-markdown-editor]").forEach((textarea) => {
        const alreadyFocused = document.activeElement === textarea;

        const editor = new EasyMDE({
            element: textarea,
            forceSync: true, // 입력할 때마다 textarea 에도 바로 반영 (폼 전송 시 내용 누락 방지)
            spellChecker: false, // 영어 맞춤법 검사기라 한글에 빨간 줄이 생긴다.
            autoDownloadFontAwesome: false, // 툴바 아이콘(Font Awesome)은 layout.html 에서 cdnjs 로 불러온다.
            status: false,
            minHeight: "200px",
            placeholder: "마크다운으로 작성할 수 있습니다. (미리보기: 눈 아이콘)",
            toolbar: ["bold", "italic", "heading", "|", "quote", "code", "unordered-list", "ordered-list", "|",
                "link", "table", "|", "preview", "side-by-side", "|", "guide"],
        });

        // textarea 가 숨겨져서 focus() 가 동작하지 않는다 → 편집기로 포커스를 넘긴다.
        // (form_errors 의 "첫 번째 에러 칸에 포커스", autofocus 속성이 그대로 동작하도록)
        textarea.focus = () => editor.codemirror.focus();
        if (alreadyFocused || textarea.hasAttribute("autofocus")) editor.codemirror.focus();
    });
});
