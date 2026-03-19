package com.prototype.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String projectName;
    private String contactEmail;
    private String contactPhone;

    private Double monthlyFee;
    private Double adBudget;

    private LocalDate contractStart;
    private LocalDate contractEnd;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Meta Ads Integration
    private String metaAdAccountId; // e.g. 957863858097757 (without act_ prefix)

    public enum Status {
        ACTIVE, PAUSED, COMPLETED
    }
}