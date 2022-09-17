package com.semicolon.goodreadsbytope.services;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.semicolon.goodreadsbytope.data.models.MailResponse;
import com.semicolon.goodreadsbytope.data.models.VerificationMessageRequest;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<MailResponse> sendHtmlMail(VerificationMessageRequest messageRequest) throws UnirestException;
    CompletableFuture<MailResponse> sendSimpleMail(VerificationMessageRequest messageRequest) throws UnirestException;
}


