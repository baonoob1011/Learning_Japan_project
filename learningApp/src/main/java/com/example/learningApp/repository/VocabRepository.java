package com.example.learningApp.repository;

import com.example.learningApp.entity.Vocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, String> {
    // Tìm từ gốc chính xác
    Optional<Vocab> findBySurface(String surface);
    @Query("SELECT v FROM Vocab v JOIN v.videos vid WHERE vid.id = :videoId")
    List<Vocab> findAllByVideoId(@Param("videoId") String videoId);
    // Nếu muốn tìm theo partial match (vd: contains)

    @Query("""
        SELECT v
        FROM Vocab v
        JOIN v.users u
        JOIN v.videos vid
        WHERE u.id = :userId
          AND vid.id = :videoId
    """)
    List<Vocab> findSavedVocabsByUserAndVideo(
            @Param("userId") String userId,
            @Param("videoId") String videoId
    );
}

