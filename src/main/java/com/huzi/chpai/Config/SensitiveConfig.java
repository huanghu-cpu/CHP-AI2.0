package com.huzi.chpai.Config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "sensitive")
@Data
public class SensitiveConfig {

    private List<String> words = new ArrayList<>();
}
