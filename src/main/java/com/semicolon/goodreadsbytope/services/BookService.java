package com.semicolon.goodreadsbytope.services;

import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.BookItemUploadRequest;
import com.semicolon.goodreadsbytope.data.models.Book;
import com.semicolon.goodreadsbytope.data.models.Credentials;
import com.semicolon.goodreadsbytope.dtos.BookDto;
import com.semicolon.goodreadsbytope.exceptions.GoodReadsException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface BookService {
    CompletableFuture<Map<String, Credentials>> generateUploadURLs(String fileExtension, String imageExtension) throws ExecutionException, InterruptedException;
    Book save(BookItemUploadRequest bookItemUploadRequest);
    Book findBookByTitle(String title) throws GoodReadsException;
    Map<String, String> generateDownloadUrls(String fileName, String imageFileName) throws GoodReadsException;
    Map<String, Object> findAll(int pageNumber, int noOfItems);

    List<BookDto> getAllBooksForUser(String email);
}

