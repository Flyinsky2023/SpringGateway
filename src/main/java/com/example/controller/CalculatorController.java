package com.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

/**
 * 计算器控制器
 * 提供基本的数学运算接口
 */
@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {
    
    /**
     * 减法运算
     * @param a 被减数
     * @param b 减数
     * @return 运算结果
     */
    @GetMapping("/subtract")
    public ResponseEntity<Map<String, Object>> subtract(
            @RequestParam @NotNull(message = "参数a不能为空") Double a,
            @RequestParam @NotNull(message = "参数b不能为空") Double b) {
        double result = a - b;
        return createSuccessResponse("减法运算", a, b, result, "-");
    }
    
    /**
     * 乘法运算
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 运算结果
     */
    @GetMapping("/multiply")
    public ResponseEntity<Map<String, Object>> multiply(
            @RequestParam @NotNull(message = "参数a不能为空") Double a,
            @RequestParam @NotNull(message = "参数b不能为空") Double b) {
        double result = a * b;
        return createSuccessResponse("乘法运算", a, b, result, "*");
    }
    
    /**
     * 除法运算
     * @param a 被除数
     * @param b 除数
     * @return 运算结果
     */
    @GetMapping("/divide")
    public ResponseEntity<Map<String, Object>> divide(
            @RequestParam @NotNull(message = "参数a不能为空") Double a,
            @RequestParam @NotNull(message = "参数b不能为空") Double b) {
        if (b == 0) {
            return createErrorResponse("除法运算错误", "除数不能为零");
        }
        double result = a / b;
        return createSuccessResponse("除法运算", a, b, result, "/");
    }
    
    /**
     * 幂运算
     * @param base 底数
     * @param exponent 指数
     * @return 运算结果
     */
    @GetMapping("/power")
    public ResponseEntity<Map<String, Object>> power(
            @RequestParam @NotNull(message = "底数不能为空") Double base,
            @RequestParam @NotNull(message = "指数不能为空") Double exponent) {
        // 限制指数大小，防止过大计算
        if (Math.abs(exponent) > 1000) {
            return createErrorResponse("幂运算错误", "指数绝对值不能超过1000");
        }
        
        double result = Math.pow(base, exponent);
        return createSuccessResponse("幂运算", base, exponent, result, "^");
    }
    
    /**
     * 平方根运算
     * @param number 数字
     * @return 运算结果
     */
    @GetMapping("/sqrt")
    public ResponseEntity<Map<String, Object>> sqrt(
            @RequestParam @NotNull(message = "参数不能为空") Double number) {
        if (number < 0) {
            return createErrorResponse("平方根运算错误", "负数不能开平方根");
        }
        double result = Math.sqrt(number);
        return createSuccessResponse("平方根运算", number, result, "√");
    }
    
    /**
     * 批量运算接口
     * @param request 运算请求
     * @return 批量运算结果
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchOperations(
            @Valid @RequestBody BatchOperationRequest request) {
        try {
            String operation = request.getOperation();
            double a = request.getA();
            double b = request.getB();
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", operation);
            result.put("a", a);
            result.put("b", b);
            
            switch (operation.toLowerCase()) {
                case "add":
                    result.put("result", a + b);
                    result.put("operator", "+");
                    break;
                case "subtract":
                    result.put("result", a - b);
                    result.put("operator", "-");
                    break;
                case "multiply":
                    result.put("result", a * b);
                    result.put("operator", "*");
                    break;
                case "divide":
                    if (b == 0) {
                        return createErrorResponse("批量运算错误", "除数不能为零");
                    }
                    result.put("result", a / b);
                    result.put("operator", "/");
                    break;
                case "power":
                    if (Math.abs(b) > 1000) {
                        return createErrorResponse("幂运算错误", "指数绝对值不能超过1000");
                    }
                    result.put("result", Math.pow(a, b));
                    result.put("operator", "^");
                    break;
                default:
                    return createErrorResponse("批量运算错误", "不支持的运算类型: " + operation);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "批量运算成功");
            response.put("data", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("批量运算错误", e.getMessage());
        }
    }
    
    /**
     * 创建成功响应
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(
            String operation, double a, double b, double result, String operator) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", operation);
        data.put("a", a);
        data.put("b", b);
        data.put("result", result);
        data.put("operator", operator);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "运算成功");
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建成功响应（单参数运算）
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(
            String operation, double a, double result, String operator) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", operation);
        data.put("number", a);
        data.put("result", result);
        data.put("operator", operator);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "运算成功");
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            String operation, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", operation + "失败");
        response.put("error", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 批量运算请求对象
     */
    public static class BatchOperationRequest {
        @NotNull(message = "运算类型不能为空")
        private String operation;
        
        @NotNull(message = "参数a不能为空")
        private Double a;
        
        @NotNull(message = "参数b不能为空")
        private Double b;

        // Getters and Setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Double getA() { return a; }
        public void setA(Double a) { this.a = a; }
        public Double getB() { return b; }
        public void setB(Double b) { this.b = b; }
    }
}