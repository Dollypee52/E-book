package com.semicolon.goodreadsbytope.data.repositories;

import com.semicolon.goodreadsbytope.data.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookRepository extends MongoRepository <Book,String> {
    Book findBookByTitleIsIgnoreCase(String title);

    List<Book> findBookByUploadedBy(String email);

    @Override
    Page<Book> findAll(Pageable pageable);
}
