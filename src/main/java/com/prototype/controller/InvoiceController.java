package com.prototype.controller;

import com.prototype.model.Invoice;
import com.prototype.model.LineItem;
import com.prototype.repository.ClientRepository;
import com.prototype.repository.InvoiceRepository;
import com.prototype.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    // --- List ---
    @GetMapping
    public String list(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAll());
        return "invoices";
    }

    // --- New Invoice Form ---
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("nextInvoiceNumber", generateInvoiceNumber());
        return "invoice-form";
    }

    // --- Save Invoice ---
    @PostMapping("/save")
    public String save(
            @RequestParam Long clientId,
            @RequestParam String issueDate,
            @RequestParam List<String> descriptions,
            @RequestParam List<Integer> quantities,
            @RequestParam List<Double> unitPrices) {

        Invoice invoice = new Invoice();
        invoice.setClient(clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found")));
        invoice.setIssueDate(LocalDate.parse(issueDate));
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(Invoice.Status.UNPAID);

        // Calculate totals
        double subtotal = 0;
        List<LineItem> items = new ArrayList<>();
        for (int i = 0; i < descriptions.size(); i++) {
            if (descriptions.get(i).isBlank()) continue;
            LineItem item = new LineItem();
            item.setDescription(descriptions.get(i));
            item.setQuantity(quantities.get(i));
            item.setUnitPrice(unitPrices.get(i));
            item.setInvoice(invoice);
            items.add(item);
            subtotal += quantities.get(i) * unitPrices.get(i);
        }

        double gst = subtotal * 0.18;
        invoice.setGstAmount(gst);
        invoice.setTotalAmount(subtotal + gst);
        invoice.setLineItems(items);

        invoiceRepository.save(invoice);
        return "redirect:/invoices";
    }

    // --- Toggle Paid/Unpaid ---
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(invoice.getStatus() == Invoice.Status.PAID
                ? Invoice.Status.UNPAID
                : Invoice.Status.PAID);
        invoiceRepository.save(invoice);
        return "redirect:/invoices";
    }

    // --- Auto-generate invoice number ---
    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        List<Invoice> all = invoiceRepository.findAll();
        long count = all.stream()
                .filter(i -> i.getInvoiceNumber() != null && i.getInvoiceNumber().startsWith(prefix))
                .count();
        return prefix + String.format("%03d", count + 1);
    }
}