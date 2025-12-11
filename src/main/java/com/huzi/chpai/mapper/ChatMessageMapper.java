package com.huzi.chpai.mapper;

import com.huzi.chpai.entity.ChatMessage;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
