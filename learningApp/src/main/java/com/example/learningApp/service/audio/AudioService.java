package com.example.learningApp.service.audio;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class AudioService {

    public File cutAudio(
            File sourceAudio,
            int startMs,
            int endMs,
            String outputName
    ) throws IOException, InterruptedException {

        double startSec = startMs / 1000.0;
        double durationSec = Math.max((endMs - startMs) / 1000.0, 0.3);

        File outFile = new File(outputName);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-ss", String.valueOf(startSec),
                "-i", sourceAudio.getAbsolutePath(),
                "-t", String.valueOf(durationSec),
                "-acodec", "libmp3lame",
                "-ab", "128k",
                outFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.waitFor();

        if (!outFile.exists() || outFile.length() == 0) {
            throw new RuntimeException("Cut audio failed: " + outFile.getName());
        }

        return outFile;
    }

    public File downloadAudio(String youtubeUrl) throws IOException, InterruptedException {

        String fileName = "audio_" + System.currentTimeMillis() + ".mp3";

        String ytDlpPath = System.getenv("YT_DLP_PATH");
        String ffmpegPath = System.getenv("FFMPEG_PATH");

        if (ytDlpPath == null) {
            ytDlpPath = Paths.get("tool", "yt-dlp.exe").toAbsolutePath().toString();
        }
        if (ffmpegPath == null) {
            ffmpegPath = Paths.get("tool", "ffmpeg.exe").toAbsolutePath().toString();
        }

        if (!Files.exists(Paths.get(ytDlpPath))) {
            throw new RuntimeException("yt-dlp not found");
        }

        ProcessBuilder pb = new ProcessBuilder(
                ytDlpPath,
                "-x", "--audio-format", "mp3",
                "--ffmpeg-location", ffmpegPath,
                "-o", fileName,
                youtubeUrl
        );

        pb.inheritIO();
        int exitCode = pb.start().waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp failed");
        }

        File audio = new File(fileName);
        if (!audio.exists()) {
            throw new RuntimeException("Audio not created");
        }

        return audio;
    }
}
