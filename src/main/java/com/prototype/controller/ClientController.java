package com.prototype.controller;

import com.prototype.model.Client;
import com.prototype.repository.NoteRepository;
import com.prototype.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private NoteRepository noteRepository;

    @GetMapping
    public String clients(Model model) {
        model.addAttribute("clients", clientService.getAll());
        model.addAttribute("newClient", new Client());
        return "clients";
    }

    @PostMapping("/add")
    public String addClient(@ModelAttribute Client client) {
        clientService.save(client);
        return "redirect:/clients";
    }

    @GetMapping("/{id}")
    public String clientDetail(@PathVariable Long id, Model model) {
        Client client = clientService.getById(id);
        model.addAttribute("client", client);
        model.addAttribute("notes", noteRepository.findByClient(client));
        return "client-detail";
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String editClientForm(@PathVariable Long id, Model model) {
        model.addAttribute("clients", clientService.getAll());
        model.addAttribute("editClient", clientService.getById(id));
        return "clients";
    }

    @PostMapping("/update/{id}")
    public String updateClient(@PathVariable Long id, @ModelAttribute Client client) {
        client.setId(id);
        clientService.save(client);
        return "redirect:/clients";
    }
}