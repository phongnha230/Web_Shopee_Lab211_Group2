package com.shoppeclone.backend.shipper.dto;

import lombok.Data;

@Data
public class DeliveryUpdateRequest {
    private String note; // Delivery note from shipper
    private String proofOfDeliveryUrl; // Image URL of delivery proof
    private String failureReason; // Reason if delivery failed
}
