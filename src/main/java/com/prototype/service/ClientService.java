package com.prototype.service;

import com.prototype.model.Client;
import com.prototype.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public void save(Client client) {
        clientRepository.save(client);
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }
}
