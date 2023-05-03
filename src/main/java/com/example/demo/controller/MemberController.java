package com.example.demo.controller;

import com.example.demo.dto.MemberSearchCondition;
import com.example.demo.dto.MemberTeamDto;
import com.example.demo.repository.MemberJpaRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequiredArgsConstructor
public class MemberController {


    private final SseEmitters sseEmitters;
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, @PageableDefault
    Pageable pageable) {
        return memberRepository.searchPageComplex(condition,pageable);
    }


    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter( 30 * 1000L);
        sseEmitters.add(emitter);

        return emitter;
    }

    @GetMapping("/hello")
    public String hello() {
        System.out.println("hello: "+Thread.currentThread().getName());
        return "hello";
    }

    @ResponseBody
    @GetMapping(value = "/hellov2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String hello2(@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        sseEmitters.send();
        System.out.println("hellov2: "+Thread.currentThread().getName());
        return "hello";
    }
}


