package com.huzi.chpai.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ChatMemoryService {

    List<Message> getMessagesByConversationId(String conversationId);

    void saveMessages(String conversationId, List<Message> messages);

    List<Message> getLastNMessages(String conversationId, int lastN);

    void deleteMessagesByConversationId(String conversationId);

}
