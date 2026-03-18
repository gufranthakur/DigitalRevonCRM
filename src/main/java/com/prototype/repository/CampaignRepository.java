package com.prototype.repository;

import com.prototype.model.Campaign;
import com.prototype.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByClient(Client client);
}
