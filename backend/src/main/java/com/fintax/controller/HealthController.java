package com.fintax.controller;

import com.fintax.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 *
 * <p>提供应用健康状态检查接口，用于监控和部署验证</p>
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     *
     * <p>用于验证应用是否正常运行</p>
     *
     * @return Result 包含 "ok" 字符串的成功响应
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }

}
