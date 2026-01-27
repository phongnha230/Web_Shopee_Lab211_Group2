package com.shoppeclone.backend.dispute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "dispute_images")
@Data
public class DisputeImage {
    @Id
    private String id;

    private String disputeId;
    private String imageUrl;
    private String uploadedBy; // buyerId or sellerId

    private LocalDateTime createdAt;

    public DisputeImage() {
        this.createdAt = LocalDateTime.now();
    }
}
