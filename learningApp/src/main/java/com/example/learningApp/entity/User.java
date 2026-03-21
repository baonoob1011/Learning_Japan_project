package com.example.learningApp.entity;

import com.example.learningApp.enums.JLPTLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "roles")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "videoTrackings", "courses", "savedVideos",
                "userExamResults", "notifications", "vocabProgresses", "learningProgresses", "savedVocabs" })
public class User {

        @Id
        private String id;

        @Column(nullable = false, unique = true, length = 255)
        private String email;

        @Column(name = "full_name", length = 255)
        private String fullName;

        @Column(name = "avatar_url", length = 255)
        private String avatarUrl;

        @Column(name = "vip_expired_at")
        private LocalDateTime vipExpiredAt;

        @Enumerated(EnumType.STRING)
        private JLPTLevel level;

        @Column(nullable = false)
        @Builder.Default
        private Boolean enabled = true;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;
        @Column(name = "last_vocab_reminder_date")
        private LocalDate lastVocabReminderDate;

        @Column(name = "last_reminder_sent_at")
        private LocalDateTime lastReminderSentAt;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @Builder.Default
        Set<UserVideoTracking> videoTrackings = new HashSet<>();

        @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @Builder.Default
        Set<Course> courses = new HashSet<>();

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        @Builder.Default
        private Set<Role> roles = new HashSet<>();

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_videos", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "video_id"))
        @Builder.Default
        private Set<YoutubeVideo> savedVideos = new HashSet<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private Set<UserExamResult> userExamResults = new HashSet<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private Set<Notification> notifications = new HashSet<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private Set<UserVocabProgress> vocabProgresses = new HashSet<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private Set<UserLearningProgress> learningProgresses = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "user_vocab", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "vocab_id"))
        @Builder.Default
        private Set<Vocab> savedVocabs = new HashSet<>();

        @PrePersist
        protected void onCreate() {
                this.createdAt = LocalDateTime.now();
                this.updatedAt = this.createdAt;
        }

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }

}

