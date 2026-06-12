package com.example.polymarket.controller;

import com.example.polymarket.service.LiveEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    private final LiveEventService liveEventService;

    public StreamController(LiveEventService liveEventService) {
        this.liveEventService = liveEventService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return liveEventService.subscribe();
    }
}
