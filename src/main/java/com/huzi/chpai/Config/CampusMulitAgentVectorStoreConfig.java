package com.huzi.chpai.Config;

import com.huzi.chpai.rag.CampusMulitAgentDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CampusMulitAgentVectorStoreConfig {
    @Resource
    private CampusMulitAgentDocumentLoader CampusMulitAgentDocumentLoader;

    @Bean
    VectorStore CampusMulitAgentVectorStoreConfig(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documents = CampusMulitAgentDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
