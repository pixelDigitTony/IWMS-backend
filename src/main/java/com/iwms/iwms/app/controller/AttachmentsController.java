package com.iwms.iwms.app.controller;

import java.net.URL;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwms.iwms.application.ports.AttachmentStoragePort;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/attachments")
@Validated
public class AttachmentsController {

    record PresignRequest(@NotBlank String key, @NotBlank String contentType, @Positive long ttlSeconds) {}
    record PresignResponse(String url) {}

    private final AttachmentStoragePort storagePort;

    public AttachmentsController(AttachmentStoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @PostMapping("/presign")
    @PreAuthorize("hasAuthority('attachments.upload')")
    public ResponseEntity<PresignResponse> presign(@RequestBody PresignRequest body) {
        URL url = storagePort.createPresignedUploadUrl(body.key(), body.contentType(), body.ttlSeconds());
        return ResponseEntity.ok(new PresignResponse(url.toString()));
    }
}


