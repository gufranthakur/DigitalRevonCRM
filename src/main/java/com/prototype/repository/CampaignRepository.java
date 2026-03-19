package com.prototype.repository;

import com.prototype.model.Campaign;
import com.prototype.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByClient(Client client);
    Optional<Campaign> findByMetaCampaignId(String metaCampaignId);
}