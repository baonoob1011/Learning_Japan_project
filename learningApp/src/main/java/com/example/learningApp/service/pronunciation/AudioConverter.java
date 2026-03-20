package com.example.learningApp.service.pronunciation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Component
public class AudioConverter {

    public File convert(MultipartFile multipartFile) {
        try {
            String originalName = multipartFile.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".tmp";

            File raw = File.createTempFile("pron_raw_", ext);
            File normalized = File.createTempFile("pron_", ".wav");

            multipartFile.transferTo(raw);

            String ffmpegPath = new File("tool/ffmpeg.exe").getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", raw.getAbsolutePath(),
                    "-ac", "1",
                    "-ar", "16000",
                    "-vn",
                    "-acodec", "pcm_s16le",
                    normalized.getAbsolutePath()
            );

            pb.redirectErrorStream(true);

            int exit = pb.start().waitFor();
            if (exit != 0) throw new RuntimeException("ffmpeg convert failed");

            raw.delete();
            return normalized;

        } catch (Exception e) {
            throw new RuntimeException("Convert MultipartFile failed", e);
        }
    }
}
