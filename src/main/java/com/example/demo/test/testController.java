package com.example.demo.test;

import com.example.demo.testGenfile.service.GenfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class testController {

    private final GenfileService genfileService;

    @GetMapping("/")
    public String showHome() {
        return "index";
    }
}
