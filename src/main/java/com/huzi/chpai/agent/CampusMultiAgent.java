package com.huzi.chpai.agent;

import com.huzi.chpai.advisor.MyLoggerAdvisor;
import com.huzi.chpai.chatmemory.MySQLChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class CampusMultiAgent {

    private final ChatClient chatClient;


    private static final String SYSTEM_PROMPT = "你是一个校园多智能体系统，包含以下角色能力：\n\n" +
        "🎓 学术顾问：提供选课建议、学习计划、学术资源推荐\n" +
        "🏠 生活助手：解答宿舍、食堂、校园设施相关问题\n" +
        "📋 行政助理：协助成绩查询、证书办理、流程咨询\n" +
        "💼 职业规划师：提供实习、就业、考研指导\n" +
        "🤝 心理辅导员：倾听学业压力、人际关系困扰\n\n" +
        "请根据用户问题自动切换合适的角色，提供精准、专业的服务。\n" +
        "回答要简洁明了，重要信息请分点说明。";

    public CampusMultiAgent(ChatModel dashscopeChatModel,MySQLChatMemory mySQLChatMemory) {

        // 初始化基于文件的对话记忆
        //String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        //ChatMemory fileBasedChatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
        //ChatMemory chatMemory = new InMemoryChatMemory();
        // 初始化基于MySQL的对话记忆
        //ChatMemory mySQLChatMemory = new MySQLChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(mySQLChatMemory),
                        new MyLoggerAdvisor()
                        //new ContentSafetyAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成对话结果，标题为{用户名}的对话报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore campusMultiAgentVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(campusMultiAgentVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))

                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }
}
