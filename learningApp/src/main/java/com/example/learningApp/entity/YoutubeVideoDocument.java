package com.example.learningApp.entity;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.time.Instant;
import java.time.LocalDateTime;
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "youtube_videos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeVideoDocument {

    @Id
    private String id;

    // 🔥 search as you type
    @Field(type = FieldType.Search_As_You_Type)
    private String title;

    private String urlVideo;

    @Field(type = FieldType.Keyword)
    private String videoTag;

    @Field(type = FieldType.Keyword)
    private String level;

    @Field(type = FieldType.Long)
    private Long createdAt;

    private String duration;

}
