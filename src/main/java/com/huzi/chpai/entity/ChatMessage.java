package com.huzi.chpai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Table("chat_messages")
public class ChatMessage  {

    @Id
    private Long id;

    @Column("conversation_id")
    private String conversationId;

    @Column("message_type")
    private String messageType;

    @Column("content")
    private String content;

    @Column("message_properties")
    private String messageProperties; // JSON字符串格式

    @Column("created_time")
    private LocalDateTime createdTime;

    @Column("updated_time")
    private LocalDateTime updatedTime;

    @Column("is_deleted")
    private Integer isDeleted;


    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", conversationId='" + conversationId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", messageProperties='" + messageProperties + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}