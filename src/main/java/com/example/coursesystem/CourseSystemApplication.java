package com.example.coursesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// 重点：加上 (exclude = ...) 这一段，关闭安全验证
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CourseSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseSystemApplication.class, args);
    }
}