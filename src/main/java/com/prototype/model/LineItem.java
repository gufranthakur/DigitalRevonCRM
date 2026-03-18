package com.prototype.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "line_items")
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Integer quantity;
    private Double unitPrice;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}