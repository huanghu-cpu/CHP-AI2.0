package com.huzi.chpai.advisor;

import com.huzi.chpai.Tool.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 内容安全Advisor
 * 在AI对话前进行用户消息的安全检查
 */
@Slf4j
@Component
public class ContentSafetyAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String ERROR_MESSAGE = "您的输入包含敏感词汇，请调整后重试。";

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Optional<AdvisedRequest> before(AdvisedRequest request) {

        String userText = request.userText();
        log.info("ContentSafetyAdvisor-检查用户输入是否包含违禁词: {}", userText);

        if(BaseTool.isSensitive(userText)){
            log.warn("ContentSafetyAdvisor-用户输入包含违禁词: {}", userText);
            return Optional.empty(); // 检测到违禁词，返回empty
        }
        return Optional.of(request);
    }


    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

        Optional<AdvisedRequest> checkedRequest = before(advisedRequest);

        if (checkedRequest.isEmpty()) {
            AssistantMessage assistantMessage = new AssistantMessage(ERROR_MESSAGE);
            Generation generation = new Generation(assistantMessage);
            ChatResponse chatResponse = new ChatResponse(List.of(generation));

            // 构建AdvisedResponse
            return AdvisedResponse.builder()
                    .response(chatResponse)
                    .adviseContext(Map.of("blocked", true))  // 一行搞定
                    .build();
        }

        // 继续调用链
        return chain.nextAroundCall(checkedRequest.get());
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        Optional<AdvisedRequest> checkedRequest = before(advisedRequest);

        if (checkedRequest.isEmpty()) {
            AssistantMessage assistantMessage = new AssistantMessage(ERROR_MESSAGE);
            Generation generation = new Generation(assistantMessage);
            ChatResponse chatResponse = new ChatResponse(List.of(generation));

            // 构建AdvisedResponse
            AdvisedResponse advisedResponse= AdvisedResponse.builder()
                    .response(chatResponse)
                    .adviseContext(Map.of("blocked", true))  // 一行搞定
                    .build();
            return Flux.just(advisedResponse);
        }

        // 继续调用链
        return chain.nextAroundStream(checkedRequest.get());
    }

}