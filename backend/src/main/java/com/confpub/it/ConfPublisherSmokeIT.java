package com.confpub.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.BeforeAll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Поднимаем полноценный контекст + MockMvc
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ConfPublisherSmokeIT {

    // Testcontainers PostgreSQL
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("confpublisher")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @BeforeAll
    static void initProps() {
        // Подсовываем Spring’у настройки datasource из контейнера
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        // Макс. размер файла из application.yml уже подходит (10MB)
        // Хранилище attachments: по умолчанию data/attachments в модуле backend
        System.setProperty("app.storage.base-dir", "build/test-attachments"); // изолируем для теста
    }

    @Test
    void full_smoke_flow() throws Exception {
        // 1) Создаём страницу
        String createBody = """
            {
              "title": "IT page",
              "content": "Hello from IT",
              "spaceKey": "TEST",
              "parentPageId": null,
              "attachmentIds": []
            }
            """;
        String createResp = mvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode page = om.readTree(createResp);
        long pageId = page.get("id").asLong();

        // 2) Загружаем файл (multipart) — ВАЖНО: путь /api/attachments (без /upload)
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes()
        );
        MockMultipartFile desc = new MockMultipartFile(
                "description", "", "text/plain", "integration test".getBytes()
        );
        String uploadResp = mvc.perform(multipart("/api/attachments")
                        .file(file).file(desc))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode att = om.readTree(uploadResp);
        long attachmentId = att.get("id").asLong();

        // 3) Привязываем вложение к странице
        String bindBody = """
            { "attachmentIds": [%d] }
            """.formatted(attachmentId);
        String detailsAfterBind = mvc.perform(post("/api/pages/{id}/attachments", pageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bindBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode details = om.readTree(detailsAfterBind);
        assertThat(details.get("attachments")).isNotNull();
        assertThat(details.get("attachments").size()).isEqualTo(1);

        // 4) Publish (stub) + status
        String publishResp = mvc.perform(post("/api/pages/{id}/publish", pageId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode pub = om.readTree(publishResp);
        assertThat(pub.get("status").asText()).isNotBlank();

        String statusResp = mvc.perform(get("/api/pages/{id}/publish/status", pageId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode stat = om.readTree(statusResp);
        assertThat(stat.get("status").asText()).isNotBlank();

        // 5) Детали страницы — вложение присутствует
        String finalDetails = mvc.perform(get("/api/pages/{id}", pageId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode fd = om.readTree(finalDetails);
        assertThat(fd.get("attachments").size()).isEqualTo(1);
    }
}