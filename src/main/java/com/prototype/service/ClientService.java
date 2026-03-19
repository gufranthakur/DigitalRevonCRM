package com.prototype.service;

import com.prototype.model.Campaign;
import com.prototype.model.Client;
import com.prototype.model.Invoice;
import com.prototype.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private NoteRepository noteRepository;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public void save(Client client) {
        clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Delete line items for each invoice first
        List<Invoice> invoices = invoiceRepository.findByClient(client);
        for (Invoice invoice : invoices) {
            lineItemRepository.deleteAll(lineItemRepository.findByInvoice(invoice));
        }

        // Then delete invoices, campaigns, notes
        invoiceRepository.deleteAll(invoices);
        campaignRepository.deleteAll(campaignRepository.findByClient(client));
        noteRepository.deleteAll(noteRepository.findByClient(client));

        // Now safe to delete client
        clientRepository.deleteById(id);
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }
}