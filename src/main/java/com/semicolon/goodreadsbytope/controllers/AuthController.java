package com.semicolon.goodreadsbytope.controllers;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.ApiResponse;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.AuthToken;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.LoginRequest;
import com.semicolon.goodreadsbytope.data.models.User;
import com.semicolon.goodreadsbytope.dtos.UserDto;
import com.semicolon.goodreadsbytope.exceptions.GoodReadsException;
import com.semicolon.goodreadsbytope.security.jwt.TokenProvider;
import com.semicolon.goodreadsbytope.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody @Valid @NotNull AccountCreationRequest accountCreationRequest) throws UnirestException, GoodReadsException, ExecutionException, InterruptedException {
        String host = request.getRequestURL().toString();
        int index = host.indexOf("/", host.indexOf("/", host.indexOf("/"))+2);
        host = host.substring(0, index+1);
        log.info("Host --> {}", host);
        UserDto userDto = userService.createUserAccount(host,accountCreationRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("user created successfully")
                .data(userDto)
                .build();
        log.info("Returning response");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }


    @RequestMapping("/verify/{token}")
    public ModelAndView verify(@PathVariable("token") String token) throws GoodReadsException {
        userService.verifyUser(token);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("verification_success");
        return modelAndView;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws GoodReadsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                        loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = tokenProvider.generateJWTToken(authentication);
        User user = userService.findUserByEmail(loginRequest.getEmail());
        return new ResponseEntity<>(new AuthToken(token, user.getId()), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }));
        return errors;
    }
}
