// dto/response/user/UserChatResponse.java
package com.example.learningApp.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChatResponse {
    private String id;
    private String fullName;
    private String avatarUrl;
}
