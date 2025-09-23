package com.example.springproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/adminDashboard")
    public String adminDashboard() {
        return "adminDashboard"; // adminDashboard.html을 templates 폴더에 두었을 때
    }
}
