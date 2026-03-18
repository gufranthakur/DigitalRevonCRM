package com.prototype.repository;

import com.prototype.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByStatus(Client.Status status);
}
