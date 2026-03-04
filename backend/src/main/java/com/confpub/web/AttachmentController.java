package com.confpub.web;

import com.confpub.domain.Attachment;
import com.confpub.repository.AttachmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final Path STORAGE_ROOT = Path.of("data", "attachments");

    private final AttachmentRepository attachmentRepository;

    public AttachmentController(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File is too large. Max 10MB.");
        }

        Files.createDirectories(STORAGE_ROOT);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "attachment");

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedName = UUID.randomUUID() + extension;
        Path destination = STORAGE_ROOT.resolve(storedName);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = new Attachment();
        attachment.setFilename(originalFilename);
        attachment.setContentType(
                Optional.ofNullable(file.getContentType()).orElse("application/octet-stream")
        );
        attachment.setSize(file.getSize());
        attachment.setStoragePath(destination.toString());
        attachment.setDescription(description);

        Attachment saved = attachmentRepository.save(attachment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachment(@PathVariable Long id) {
        return attachmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

