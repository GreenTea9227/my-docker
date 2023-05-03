package com.example.demo.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Async("asyncExecutor")
@Component
public class SseEmitters {
    Map<String, SseEmitter> map = new HashMap<>();
    int count=0;
    public void add(SseEmitter sseEmitter) {
        System.out.println("add: "+Thread.currentThread().getName());
        log.info("add 호출");
        String id = UUID.randomUUID().toString().substring(0, 6);
        map.put(id ,sseEmitter);
        try {
            log.info("sseEmitter 호출 {}",sseEmitter);
            sseEmitter.send("hello - world" + ++count);
            // we could send more events
        } catch (Exception ex) {
            sseEmitter.completeWithError(ex);
        }
        sseEmitter.onCompletion(() -> {
            map.remove(id);
        log.info("onCompletion");});
        sseEmitter.onTimeout(() -> {
            map.remove(id);
            log.info("onTimeout");
        });
    }

    public void send() {
        System.out.println("send: "+Thread.currentThread().getName());
        for (SseEmitter value : map.values()) {
            try {
                value.send(SseEmitter
                        .event()
                                .id("123")
                        .name("send")
                        .data("send data"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
