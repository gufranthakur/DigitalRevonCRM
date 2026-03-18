package com.prototype.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private LocalDate issueDate;
    private Double totalAmount;
    private Double gstAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PAID, UNPAID
    }

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<LineItem> lineItems;
}