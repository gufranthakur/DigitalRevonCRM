package com.prototype.repository;

import com.prototype.model.Client;
import com.prototype.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClient(Client client);
}
