package com.confpub.web;

import com.confpub.domain.Attachment;
import com.confpub.repository.AttachmentRepository;

// OpenAPI / Swagger (без сложных schema-properties)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// Spring
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    // ===== Настройки загрузок (валидация) =====
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    // Разрешённые MIME-типы
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "text/plain",
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    // Разрешённые расширения (нижний регистр, с точкой)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".txt", ".pdf", ".png", ".jpg", ".jpeg"
    );

    private final AttachmentRepository attachmentRepository;

    /**
     * Базовая директория хранения берётся из application.yml:
     * app.storage.base-dir (по умолчанию data/attachments)
     */
    private final Path baseStorageDir;

    public AttachmentController(
            AttachmentRepository attachmentRepository,
            @Value("${app.storage.base-dir:data/attachments}") String baseDir
    ) {
        this.attachmentRepository = attachmentRepository;
        this.baseStorageDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    // ======== СКАЧАТЬ СОДЕРЖИМОЕ ФАЙЛА ========
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        var attOpt = attachmentRepository.findById(id);
        if (attOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var att = attOpt.get();

        // Абсолютный путь к файлу
        Path path = Paths.get(att.getStoragePath());
        if (!path.isAbsolute()) {
            path = baseStorageDir.resolve(path).normalize();
        }

        Resource resource = new FileSystemResource(path.toFile());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String filename = att.getFilename();
        String contentType = (att.getContentType() != null && !att.getContentType().isBlank())
                ? att.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    // ======== МЕТАДАННЫЕ ВЛОЖЕНИЯ (JSON) ========
    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachment(@PathVariable Long id) {
        return attachmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ======== ЗАГРУЗКА ФАЙЛА (multipart) ========
    @Operation(
            summary = "Загрузить новый файл-вложение",
            description = "Форма multipart/form-data. Поля: file (обязательное, binary), description (опционально, text)."
    )
    @ApiResponse(responseCode = "201", description = "Создано")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @Parameter(description = "Файл (обязательное поле)") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Описание файла (опционально)") @RequestPart(value = "description", required = false) String description
    ) throws IOException {

        // 1) Пусто/нет
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // 2) Лимит размера (дополнительно к spring.servlet.multipart.* в конфиге)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large. Max = 10MB");
        }

        // 3) MIME-тип из запроса (или octet-stream)
        String contentType = Optional.ofNullable(file.getContentType())
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 4) Имя файла + расширение (в нижнем регистре)
        String originalName = StringUtils.cleanPath(
                Optional.ofNullable(file.getOriginalFilename()).orElse("attachment")
        );

        // защита от path-traversal
        if (originalName.contains("..") || Paths.get(originalName).isAbsolute()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }


        String lower = originalName.toLowerCase(Locale.ROOT);
        String ext = lower.lastIndexOf('.') >= 0 ? lower.substring(lower.lastIndexOf('.')) : "";

        // 5) Белые списки: и тип, и расширение должны быть разрешены
        boolean typeOk = ALLOWED_CONTENT_TYPES.contains(contentType);
        boolean extOk = ALLOWED_EXTENSIONS.contains(ext);
        if (!(typeOk && extOk)) {
            String msg = "Unsupported file type: contentType=" + contentType + ", ext=" + ext +
                    ". Allowed: " + ALLOWED_CONTENT_TYPES + " / " + ALLOWED_EXTENSIONS;
            return ResponseEntity.badRequest().body(msg);
        }

        // 6) Подготовка директории хранения
        Files.createDirectories(baseStorageDir);

        // 7) Имя на диске: UUID + исходное расширение
        String storedName = UUID.randomUUID() + ext;
        Path destination = baseStorageDir.resolve(storedName).normalize();

        // 8) Сохранение файла и запись метаданных
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            Attachment a = new Attachment();
            a.setFilename(originalName);
            a.setContentType(contentType);
            a.setSize(file.getSize());
            // В БД храним путь как есть; при download резолвим относительно baseStorageDir, если он относительный
            a.setStoragePath(destination.toString());
            a.setDescription(description);

            Attachment saved = attachmentRepository.save(a);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IOException ex) {
            try { Files.deleteIfExists(destination); } catch (IOException ignored) {}
            throw ex;
        }
    }
}

