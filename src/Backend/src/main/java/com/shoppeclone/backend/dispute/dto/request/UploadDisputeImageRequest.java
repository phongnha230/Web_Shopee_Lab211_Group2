package com.shoppeclone.backend.dispute.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadDisputeImageRequest {
    @NotBlank(message = "imageUrl is required")
    private String imageUrl;
}
