package dev.frizio.lab.repository;

import org.springframework.data.repository.ListCrudRepository;

import dev.frizio.lab.model.Book;

public interface BookRepository extends ListCrudRepository<Book, Long> {
    
}
