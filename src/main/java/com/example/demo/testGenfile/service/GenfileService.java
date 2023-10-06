package com.example.demo.testGenfile.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class GenfileService {


    public void save(MultipartFile testVideo) {

        File file = new File("C:\\mydir\\myfile.mp4");

        file.getParentFile().mkdirs();

        try {
            testVideo.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
