package com.semicolon.goodreadsbytope.controllers;

import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.ApiResponse;
import com.semicolon.goodreadsbytope.data.models.Book;
import com.semicolon.goodreadsbytope.data.models.Credentials;
import com.semicolon.goodreadsbytope.exceptions.GoodReadsException;
import com.semicolon.goodreadsbytope.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;
    private final List<String> validImageExtensions;
    private final List<String> validFileExtensions;

    public BookController(BookService bookService) {
        this.bookService = bookService;
        validImageExtensions = Arrays.asList(".png", ".jpg",".jpeg");
        validFileExtensions = Arrays.asList(".txt", ".pdf", ".doc", ".docx", ".csv",
                ".epub", ".xlsx");
    }

    @PostMapping("/")
    public ResponseEntity<?> uploadBookItem(@RequestBody @Valid @NotNull BookItemUploadRequest bookItemUploadRequest){
        Book book = bookService.save(bookItemUploadRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("book saved successfully")
                .data(book)
                .build();
        return  new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{pageNo}/{noOfItems}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllBooks(
            @PathVariable(value = "pageNo", required = false) @DefaultValue({"0"}) @NotNull String pageNo,
            @PathVariable(value = "noOfItems", required = false) @DefaultValue({"10"}) @NotNull String numberOfItems){

        Map<String, Object> pageResult = bookService.findAll(Integer.parseInt(pageNo), Integer.parseInt(numberOfItems));
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("pages returned")
                .data(pageResult)
                .result((int)pageResult.get("NumberOfElementsInPage"))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<?> getBookByTitle(@RequestParam @NotNull @NotBlank String title) throws GoodReadsException {
        Book book = bookService.findBookByTitle(title);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("book found")
                .data(book)
                .result(1)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.FOUND);
    }

    @GetMapping("/upload")
    public ResponseEntity<?> getUploadUrls(
            @RequestParam("fileExtension") @Valid @NotBlank @NotNull String fileExtension,
            @RequestParam("imageExtension") @Valid @NotBlank @NotNull String imageExtension) throws GoodReadsException, ExecutionException, InterruptedException {
        if (!validFileExtensions.contains(fileExtension)){
            throw new GoodReadsException("file extension not accepted", 400);
        }
        if (!validImageExtensions.contains(imageExtension)){
            throw new GoodReadsException("image extension not accepted", 400);
        }
        Map<String, Credentials> map = bookService.generateUploadURLs(fileExtension, imageExtension).get();
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("upload urls created")
                .data(map)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/download")
    public ResponseEntity<?> getDownloadUrls(
            @RequestParam("fileName") @Valid @NotBlank @NotNull String fileName,
            @RequestParam("imageFileName") @Valid @NotBlank @NotNull String imageFileName
    ) throws GoodReadsException {
        if (!validFileExtensions.contains("."+fileName.split("\\.")[1])){
            throw new GoodReadsException("file extension not accepted", 400);
        }
        if (!validImageExtensions.contains("."+imageFileName.split("\\.")[1])){
            throw new GoodReadsException("image file extension not accepted", 400);
        }
        Map<String, String> map = bookService.generateDownloadUrls(fileName, imageFileName);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("download urls created")
                .data(map)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}

