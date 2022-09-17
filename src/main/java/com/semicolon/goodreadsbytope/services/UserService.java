package com.semicolon.goodreadsbytope.services;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.AccountCreationRequest;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.UpdateRequest;
import com.semicolon.goodreadsbytope.data.models.User;
import com.semicolon.goodreadsbytope.dtos.UserDto;
import com.semicolon.goodreadsbytope.exceptions.GoodReadsException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {
    UserDto createUserAccount(String host, AccountCreationRequest accountCreationRequest) throws GoodReadsException, UnirestException, ExecutionException, InterruptedException;
    UserDto findUserById(String userId) throws GoodReadsException;
    List<UserDto> findAll();
    UserDto updateUserProfile(String id, UpdateRequest updateRequest) throws GoodReadsException;
    User findUserByEmail(String email) throws GoodReadsException;

    void verifyUser(String token) throws GoodReadsException;
}

