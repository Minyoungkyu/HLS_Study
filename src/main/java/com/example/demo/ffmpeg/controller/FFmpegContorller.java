package com.example.demo.ffmpeg.controller;

import com.example.demo.ffmpeg.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@RequiredArgsConstructor
public class FFmpegContorller {

    private final FFmpegService fFmpegService;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    private final String inputFilePath = "C:\\mydir\\myfile.mp4";

    private final String outputFilePath = "C:\\mydir\\hls";

    @GetMapping("hls-make")
    public String hlsMakes() {
        CompletableFuture.runAsync(() -> {
            try {
                this.fFmpegService.videoHlsMake();
            } catch (IOException e) {
                e.printStackTrace(); // 혹은 로깅
            }
        });
        return "index";
    }

    @GetMapping("/hls")
    public String videoHls(Model model) {
        model.addAttribute("videoUrl", "\\hls\\master.m3u8");
        return "hls";
    }

    @GetMapping("/hls/{fileName}.m3u8")
    public ResponseEntity<Resource> videoHlsMasterM3U8(@PathVariable String fileName) {

        System.out.println("호출 됨");

        String fileFullPath = outputFilePath + "/" + fileName + ".m3u8";
        Resource resource = new FileSystemResource(fileFullPath);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".m3u8");
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));

        System.out.println("실행 됨");
        
        return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/hls/{folderName2}/{fileName}.m3u8")
    public ResponseEntity<Resource> videoHlsMediaM3U8(@PathVariable String folderName2, @PathVariable String fileName) {

        System.out.println("호출 됨");

        String fileFullPath = outputFilePath + "/" + folderName2 + "/" + fileName + ".m3u8";
        Resource resource = new FileSystemResource(fileFullPath);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".m3u8");
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));

        System.out.println("실행 됨");

        return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/hls/{folderName2}/{tsName}.ts")
    public ResponseEntity<Resource> videoHlsTs(@PathVariable String folderName2, @PathVariable String tsName) {
        String fileFullPath = outputFilePath + "/" + folderName2 + "/" + tsName + ".ts";
        Resource resource = new FileSystemResource(fileFullPath);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tsName + ".ts");
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }

}
