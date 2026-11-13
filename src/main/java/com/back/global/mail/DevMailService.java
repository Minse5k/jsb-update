package com.back.global.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Profile({"dev", "test"}) // 개발·테스트에서만 빈으로 등록된다.
@Service
public class DevMailService implements MailService {
    public record SentMail(String to, String subject, String body, LocalDateTime sentAt) {
    }

    // 여러 요청이 동시에 보내도 안전한 목록
    private final List<SentMail> mails = new CopyOnWriteArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        mails.add(new SentMail(to, subject, body, LocalDateTime.now()));
    }

    // 최근에 보낸 메일부터
    public List<SentMail> findAll() {
        List<SentMail> list = new ArrayList<>(mails);
        Collections.reverse(list);
        return list;
    }
}
