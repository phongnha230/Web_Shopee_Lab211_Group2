package com.shoppeclone.backend.shipping.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shipping_providers")
@Data
public class ShippingProvider {
    @Id
    private String id;
    private String name;
    private String apiEndpoint;
}
