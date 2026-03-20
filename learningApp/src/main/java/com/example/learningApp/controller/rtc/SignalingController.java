package com.example.learningApp.controller.rtc;

import com.example.learningApp.dto.request.rtc.SignalMessageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    @MessageMapping("/signal")
    @SendTo("/topic/signal")
    public SignalMessageRequest signal(SignalMessageRequest message) {
        return message; // chỉ relay
    }
}

