package com.example.learningApp.mapper;


import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.Vocab;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;


@Mapper(componentModel = "spring")
public interface VocabMapper {
 Vocab toVocab (CreateVocabRequest request);
 VocabCache toVocabCache(Vocab vocab);

 default List<VocabCache> toVocabCacheList(Set<Vocab> vocabs) {
  return vocabs.stream().map(this::toVocabCache).toList();
 }

 default List<VocabCache> toVocabCacheList(List<Vocab> vocabs) {
  return vocabs.stream().map(this::toVocabCache).toList();
 }}
