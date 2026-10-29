package dev.frizio.lab.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.frizio.lab.dto.BookRequest;
import dev.frizio.lab.dto.BookResponse;
import dev.frizio.lab.exception.BookNotFoundException;
import dev.frizio.lab.model.Book;
import dev.frizio.lab.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = new Book(null, request.title(), request.author());
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }

        Book book = new Book(id, request.title(), request.author());
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }

        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.id(), book.title(), book.author());
    }
}
