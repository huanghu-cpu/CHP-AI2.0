package com.huzi.chpai.service.Impl;

import cn.hutool.core.date.DateTime;

import cn.hutool.json.JSONUtil;
import com.huzi.chpai.entity.ChatMessage;
import com.huzi.chpai.mapper.ChatMessageMapper;
import com.huzi.chpai.service.ChatMemoryService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatMemoryServiceImpl implements ChatMemoryService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public List<Message> getMessagesByConversationId(String conversationId) {
        // 查询未删除的对话消息，按创建时间排序
        List<ChatMessage> chatMessages = chatMessageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("conversation_id", conversationId)
                        .eq("is_deleted", 0) // 逻辑删除过滤
                        .orderBy("created_time", true) // 按创建时间升序
        );

        // 转换为Spring AI Message对象
        return chatMessages.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMessages(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        List<ChatMessage> chatMessages = messages.stream()
            .map(message -> convertToChatMessage(conversationId, message))
            .collect(Collectors.toList());

        // 使用insertBatch进行批量插入
        chatMessageMapper.insertBatch(chatMessages);
    }


    @Override
    public List<Message> getLastNMessages(String conversationId, int lastN) {
        // 查询最新的N条消息
        List<ChatMessage> chatMessages = chatMessageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("conversation_id", conversationId)
                        .eq("is_deleted", 0)
                        .orderBy("created_time", false) // 按创建时间降序
                        .limit(lastN)
        );

        // 反转顺序，保持时间升序
        Collections.reverse(chatMessages);

        return chatMessages.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessagesByConversationId(String conversationId) {
        // 逻辑删除：更新is_deleted字段为1
        ChatMessage updateEntity = new ChatMessage();
        updateEntity.setIsDeleted(1);
        updateEntity.setUpdatedTime(LocalDateTime.now());

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("conversation_id", conversationId)
                .eq("is_deleted", 0);

        chatMessageMapper.updateByQuery(updateEntity, queryWrapper);
    }

    /**
     * 将ChatMessage转换为Spring AI Message
     */
    private Message convertToMessage(ChatMessage chatMessage) {
        // 验证消息类型
        String messageTypeStr = chatMessage.getMessageType();
        if (messageTypeStr == null || messageTypeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("消息类型不能为空");
        }

        // 转换为MessageType枚举
        MessageType messageType = MessageType.fromValue(messageTypeStr);

        // 根据消息类型创建对应的Message实现类
        String content = chatMessage.getContent() != null ? chatMessage.getContent() : "";

        switch (messageType) {
            case USER:
                return new UserMessage(content);
            case ASSISTANT:
                return new AssistantMessage(content);
            case SYSTEM:
                return new SystemMessage(content);
            case TOOL:
                return convertToToolMessage(chatMessage, content);
            default:
                throw new IllegalArgumentException("不支持的MessageType: " + messageType);
        }
    }

    /**
     * 将Spring AI Message转换为ChatMessage
     */
    private ChatMessage convertToChatMessage(String conversationId, Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);

        // 设置消息类型（使用getValue()获取字符串值）
        chatMessage.setMessageType(message.getMessageType().getValue());

        // 设置内容（使用getText()获取文本内容）
        chatMessage.setContent(message.getText());

        // 处理metadata（原getProperties()）
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null && !metadata.isEmpty()) {
            chatMessage.setMessageProperties(JSONUtil.toJsonStr(metadata));
        }

        // 设置时间戳和删除标记
        chatMessage.setCreatedTime(LocalDateTime.now());
        chatMessage.setUpdatedTime(LocalDateTime.now());
        chatMessage.setIsDeleted(0);

        return chatMessage;
    }

    /**
     * 将ChatMessage转换为ToolResponseMessage
     */
    private Message convertToToolMessage(ChatMessage chatMessage, String content) {
        // 解析message_properties字段中的ToolResponse信息
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();

        if (chatMessage.getMessageProperties() != null && !chatMessage.getMessageProperties().isEmpty()) {
            try {
                // 尝试解析JSON格式的tool responses
                Map<String, Object> properties =JSONUtil.toBean(chatMessage.getMessageProperties(), Map.class);

                // 从properties中提取tool responses信息
                Object responsesObj = properties.get("responses");
                if (responsesObj instanceof List) {
                    // 这里需要根据实际存储格式进行解析
                    // 简化处理：创建一个默认的ToolResponse
                    responses.add(new ToolResponseMessage.ToolResponse(
                            "default-id",
                            "default-tool",
                            content
                    ));
                }
            } catch (Exception e) {
                // 解析失败，创建默认的ToolResponse
                System.err.println("解析ToolResponse失败: " + e.getMessage());
            }
        }

        // 如果没有解析到responses，创建默认的
        if (responses.isEmpty()) {
            responses.add(new ToolResponseMessage.ToolResponse(
                    "default-id",
                    "default-tool",
                    content
            ));
        }

        return new ToolResponseMessage(responses);
    }

}
