package com.huzi.chpai.chatmemory;


import com.huzi.chpai.service.ChatMemoryService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MySQLChatMemory implements ChatMemory {


    @Autowired
    private ChatMemoryService chatMemoryService;


    @Override
    public void add(String conversationId, List<Message> messages) {
        chatMemoryService.saveMessages(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 直接调用Service层获取最新N条消息
        return chatMemoryService.getLastNMessages(conversationId, lastN);
    }

    @Override
    public void clear(String conversationId) {
        // 调用Service层删除对话消息
        chatMemoryService.deleteMessagesByConversationId(conversationId);
    }
}
