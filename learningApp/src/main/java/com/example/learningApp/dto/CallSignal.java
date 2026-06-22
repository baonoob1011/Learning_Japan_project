package com.example.learningApp.dto;

import lombok.Data;

@Data
public class CallSignal {
    private String type; // offer | answer | ice
    private String roomId;
    private String senderId;
    private Object data;
}
