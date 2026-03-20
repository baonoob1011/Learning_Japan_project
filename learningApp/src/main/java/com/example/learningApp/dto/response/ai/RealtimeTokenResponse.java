package com.example.learningApp.dto.response.ai;
// RealtimeTokenResponse.java

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class RealtimeTokenResponse {
    private ClientSecret client_secret;

    @Data
    public static class ClientSecret {
        private String value;
        private long expires_at;
    }
}

