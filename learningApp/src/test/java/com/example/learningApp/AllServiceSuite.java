package com.example.learningApp;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.example.learningApp.service.auth",
        "com.example.learningApp.service.video",
        "com.example.learningApp.service.course",
        "com.example.learningApp.service.exam",
        "com.example.learningApp.service.kanji",
        "com.example.learningApp.service.ai",
        "com.example.learningApp.service.community",
        "com.example.learningApp.service.admin"
})
class AllServiceSuite {
    // Suite class to run all service tests in one command.
}
