package com.back.global.mail;

// 메일 보내기. 어떻게 보낼지는 구현체가 정한다.
//   개발·테스트 : DevMailService (실제로 보내지 않고 메모리에 보관, /dev/mailbox 에서 확인)
//   운영       : SMTP 구현체를 따로 만들어 바꿔 끼운다. (이 프로젝트에는 아직 없음)
public interface MailService {
    void send(String to, String subject, String body);
}
