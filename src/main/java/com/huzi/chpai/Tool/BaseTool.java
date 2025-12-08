package com.huzi.chpai.Tool;

import com.huzi.chpai.Algorithm.ACAutomaton;
import com.huzi.chpai.Config.SensitiveConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseTool {
    private static SensitiveConfig sensitiveConfig;

    @Autowired
    public void setSensitiveConfig(SensitiveConfig config) {
        BaseTool.sensitiveConfig = config;  // 设置到静态字段
    }

    public static Boolean isSensitive(String text) {
        //AC自动机检查文本是否包含违禁词
        //return ACAutomaton.isSensitive(text);

        // 基于配置文件检查文本是否包含违禁词
        if (sensitiveConfig == null) return false;
        return sensitiveConfig.getWords().stream()
                .anyMatch(word -> text.contains(word));
    }
}
