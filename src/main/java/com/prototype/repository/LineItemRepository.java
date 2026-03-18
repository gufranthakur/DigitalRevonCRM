package com.prototype.repository;

import com.prototype.model.Invoice;
import com.prototype.model.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineItemRepository extends JpaRepository<LineItem, Long> {
    List<LineItem> findByInvoice(Invoice invoice);
}
