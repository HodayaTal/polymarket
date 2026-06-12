package com.example.polymarket.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class LiveEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        sendToEmitter(emitter, "connected", Map.of("message", "connected"));
        return emitter;
    }

    public void publish(String eventName, Object body) {
        for (SseEmitter emitter : emitters) {
            sendToEmitter(emitter, eventName, body);
        }
    }

    private void sendToEmitter(SseEmitter emitter, String eventName, Object body) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(body));
        } catch (IOException | IllegalStateException ex) {
            emitters.remove(emitter);
        }
    }
}
