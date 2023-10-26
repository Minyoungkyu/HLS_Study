package com.example.demo.ffmpeg.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class FFmpegService {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    private final String inputFilePath = "C:\\mydir\\myfile.mp4";

    private final String outputFilePath = "C:\\mydir\\hls\\";

//    public void videoHlsMake() throws IOException {
//        File tsPath = new File(outputFilePath);
//        if (!tsPath.exists()) {
//            tsPath.mkdir();
//        }
//
//        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
//        FFprobe ffprobe = new FFprobe(ffprobePath);
//
//        // 각 작업에 대해 CompletableFuture를 사용하여 비동기로 작업을 수행하고 결과를 처리합니다.
//        CompletableFuture<Void> future360p = CompletableFuture.runAsync(() -> {
//            try {
//                convertVideo(ffmpeg, ffprobe, "360p.m3u8", "-2:360", "600k", "64k");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).thenRun(() -> System.out.println("360p 변환 완료"));
//
//        CompletableFuture<Void> future720p = CompletableFuture.runAsync(() -> {
//            try {
//                convertVideo(ffmpeg, ffprobe, "720p.m3u8", "-2:720", "1500k", "128k");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).thenRun(() -> System.out.println("720p 변환 완료"));
//
//        CompletableFuture<Void> future1080p = CompletableFuture.runAsync(() -> {
//            try {
//                convertVideo(ffmpeg, ffprobe, "1080p.m3u8", "-2:1080", "2800k", "128k");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).thenRun(() -> System.out.println("1080p 변환 완료"));
//
//        // 모든 작업이 완료되면 종료 메시지를 출력합니다.
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(future360p, future720p, future1080p);
//        allOf.thenRun(() -> System.out.println("모든 변환 작업 완료")).join();
//
//        allOf.thenRun(() -> {
//            System.out.println("모든 변환 작업 완료");
//            try {
//                createMasterPlaylist();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).join();
//    }

    private int getVideoHeight(String videoPath, String ffprobePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=height",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath
        );

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String heightString = reader.readLine();
            return Integer.parseInt(heightString.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void videoHlsMake() throws IOException {
        File tsPath = new File(outputFilePath);
        if (!tsPath.exists()) {
            tsPath.mkdir();
        }

        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
        FFprobe ffprobe = new FFprobe(ffprobePath);

        // 원본 영상의 해상도를 가져옵니다.
        FFmpegProbeResult probeResult = ffprobe.probe(inputFilePath); // inputFilePath는 원본 비디오 경로 변수로 가정합니다.
        int originalHeight = getVideoHeight(inputFilePath, ffprobePath);

        CompletableFuture<Void> future480p = CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, "480", "480p.m3u8", "-2:480", "1000k", "96k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> System.out.println("480p 변환 완료"));

        CompletableFuture<Void> future720p = CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, "720","720p.m3u8", "-2:720", "2800k", "128k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> System.out.println("720p 변환 완료"));

        CompletableFuture<Void> future1080p = originalHeight >= 1080 ? CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, "1080","1080p.m3u8", "-2:1080", "18000k", "128k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> System.out.println("1080p 변환 완료")) : CompletableFuture.completedFuture(null);

        // 모든 작업이 완료되면 종료 메시지를 출력합니다.
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future480p, future720p, future1080p);
        allOf.thenRun(() -> {
            System.out.println("모든 변환 작업 완료");
            try {
                createMasterPlaylist();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).join();
    }




    // 비디오 변환 작업을 메서드로 분리
    private String convertVideo(FFmpeg ffmpeg, FFprobe ffprobe, String folderName, String outputFileName, String scale, String bitrateVideo, String bitrateAudio) throws IOException {

        File outputDir = new File(outputFilePath, folderName);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        String outputPath = new File(outputDir, outputFileName).getAbsolutePath();

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFilePath)
                .addOutput(outputPath)
                .addExtraArgs("-c:v", "libx264")
                .addExtraArgs("-c:a", "aac")
                .addExtraArgs("-ar", "48000")
                .addExtraArgs("-filter:v", "scale=" + scale)
                .addExtraArgs("-b:v", bitrateVideo)
                .addExtraArgs("-b:a", bitrateAudio)
                .addExtraArgs("-profile:v", "baseline")
                .addExtraArgs("-level", "3.0")
                .addExtraArgs("-start_number", "0")
                .addExtraArgs("-hls_time", "10")
                .addExtraArgs("-hls_list_size", "0")
                .addExtraArgs("-f", "hls")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        return outputFileName + " 변환 완료";
    }

    private void createMasterPlaylist() throws IOException {
        // Master Playlist 생성 시작
        StringBuilder masterPlaylistContent = new StringBuilder("#EXTM3U\n");
        masterPlaylistContent.append("#EXT-X-VERSION:3\n");
        masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360\n");
        masterPlaylistContent.append("480/480p.m3u8\n");
        masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=1280x720\n");
        masterPlaylistContent.append("720/720p.m3u8\n");

        // 원본 해상도가 1080 이상일 때만 추가
        if (getVideoHeight(inputFilePath, ffprobePath) >= 1080) {
            masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1920x1080\n");
            masterPlaylistContent.append("1080/1080p.m3u8\n");
        }

        Path path = Paths.get(outputFilePath, "master.m3u8");
        Files.write(path, masterPlaylistContent.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Master playlist created: " + path.toString());
    }







}
