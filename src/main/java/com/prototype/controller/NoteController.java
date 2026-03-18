package com.prototype.controller;

import com.prototype.model.Note;
import com.prototype.repository.NoteRepository;
import com.prototype.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ClientService clientService;

    @PostMapping("/clients/{id}/notes")
    public String addNote(@PathVariable Long id,
                          @RequestParam String content,
                          @RequestParam Note.Type type) {

        Note note = new Note();
        note.setContent(content);
        note.setType(type);
        note.setCreatedAt(LocalDateTime.now());
        note.setClient(clientService.getById(id));

        noteRepository.save(note);
        return "redirect:/clients/" + id;
    }
}