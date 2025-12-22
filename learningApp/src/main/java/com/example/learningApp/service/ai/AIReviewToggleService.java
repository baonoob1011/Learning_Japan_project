package com.example.learningApp.service.ai;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data

public class AIReviewToggleService {
    private boolean enabled = true;
}
