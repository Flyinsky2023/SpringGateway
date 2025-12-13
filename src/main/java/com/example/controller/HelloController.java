package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello World 控制器
 */
@RestController
public class HelloController {
    
    /**
     * Hello World 接口
     * @return Hello World 字符串
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
    
    /**
     * 带参数的 Hello 接口
     * @param name 名称参数
     * @return JSON 格式响应
     */
    @GetMapping("/hello/name")
    public Map<String, Object> helloWithName(@RequestParam(defaultValue = "World") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Hello, " + name + "!");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
}
