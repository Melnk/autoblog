package com.autoblog.attachment.api;

import com.autoblog.attachment.api.dto.EventAttachmentResponse;
import com.autoblog.attachment.application.AttachmentContentView;
import com.autoblog.attachment.application.EventAttachmentService;
import com.autoblog.attachment.domain.AttachmentType;
import com.autoblog.attachment.domain.AttachmentVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments")
@Tag(name = "Event attachments")
@SecurityRequirement(name = "bearerAuth")
public class EventAttachmentController {

    private final EventAttachmentService attachments;

    public EventAttachmentController(EventAttachmentService attachments) {
        this.attachments = attachments;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an attachment to a vehicle event",
            description = "Stores evidence for a specific vehicle event. Allowed content types: image/jpeg, image/png, image/webp, application/pdf.",
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "Attachment metadata",
                    content = @Content(schema = @Schema(implementation = EventAttachmentResponse.class))
            )
    )
    public ResponseEntity<EventAttachmentResponse> uploadAttachment(
            @PathVariable UUID vehicleId,
            @PathVariable UUID eventId,
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Attachment type", example = "RECEIPT")
            @RequestParam AttachmentType type,
            @Parameter(description = "Visibility. Defaults to PRIVATE.", example = "PUBLIC")
            @RequestParam(required = false) AttachmentVisibility visibility,
            @Parameter(description = "Optional human-readable attachment description", example = "Чек за замену масла")
            @RequestParam(required = false) String description
    ) {
        EventAttachmentResponse response = EventAttachmentResponse.from(
                attachments.upload(vehicleId, eventId, file, type, visibility, description)
        );

        return ResponseEntity.created(URI.create(
                        "/api/v1/vehicles/" + vehicleId + "/events/" + eventId + "/attachments/" + response.id()
                ))
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List attachments for a vehicle event")
    public List<EventAttachmentResponse> listAttachments(
            @PathVariable UUID vehicleId,
            @PathVariable UUID eventId
    ) {
        return attachments.list(vehicleId, eventId).stream()
                .map(EventAttachmentResponse::from)
                .toList();
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(
            summary = "Download an event attachment",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Attachment bytes",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
    )
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable UUID vehicleId,
            @PathVariable UUID eventId,
            @PathVariable UUID attachmentId
    ) {
        return fileResponse(attachments.download(vehicleId, eventId, attachmentId));
    }

    private ResponseEntity<byte[]> fileResponse(AttachmentContentView content) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(content.originalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(content.content());
    }
}
