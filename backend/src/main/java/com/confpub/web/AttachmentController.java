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
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final Path STORAGE_ROOT = Path.of("data", "attachments");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "application/pdf",
            "text/plain",
            "application/octet-stream"
    );

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

        String contentType = Optional.ofNullable(file.getContentType())
                .orElse("application/octet-stream");
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported file type: " + contentType);
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

        Attachment attachment = new Attachment();
        attachment.setFilename(originalFilename);
        attachment.setContentType(contentType);
        attachment.setSize(file.getSize());
        attachment.setStoragePath(destination.toString());
        attachment.setDescription(description);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            Attachment saved = attachmentRepository.save(attachment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IOException | RuntimeException ex) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // best-effort cleanup
            }

            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw ex;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachment(@PathVariable Long id) {
        return attachmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

