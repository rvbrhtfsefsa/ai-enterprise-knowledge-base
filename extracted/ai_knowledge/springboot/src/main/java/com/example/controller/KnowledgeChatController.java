package com.example.controller;

import com.example.entity.KnowledgeChatRequest;
import com.example.service.KnowledgeChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/knowledgeChat")
public class KnowledgeChatController {

    @Resource
    private KnowledgeChatService knowledgeChatService;

    /**
     * 管理端流式问答（SSE）
     */
    @PostMapping(value = "/askStream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter askStream(@RequestBody KnowledgeChatRequest request) {
        return knowledgeChatService.chatStream(request);
    }

    /**
     * 用户端流式问答（SSE）
     */
    @PostMapping(value = "/frontAskStream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter frontAskStream(@RequestBody KnowledgeChatRequest request) {
        return knowledgeChatService.frontChatStream(request);
    }
}
