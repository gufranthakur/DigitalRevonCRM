package com.prototype.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double adSpend;
    private Integer leadsGenerated;
    private Double cpl;
    private Long impressions;
    private Long clicks;

    private LocalDate startDate;
    private LocalDate endDate;

    // Used to match campaigns on re-sync — prevents duplicates
    @Column(unique = true)
    private String metaCampaignId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}