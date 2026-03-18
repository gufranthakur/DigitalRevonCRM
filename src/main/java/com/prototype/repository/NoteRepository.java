package com.prototype.repository;

import com.prototype.model.Client;
import com.prototype.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByClient(Client client);
}
