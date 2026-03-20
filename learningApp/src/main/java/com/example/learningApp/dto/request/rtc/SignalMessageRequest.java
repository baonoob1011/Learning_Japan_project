package com.example.learningApp.dto.request.rtc;

import lombok.Data;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignalMessageRequest {

    String type;     // join | offer | answer | candidate
    String room;
    Object data;
}

