package com.semicolon.goodreadsbytope.services;

import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.AccountCreationRequest;
import com.semicolon.goodreadsbytope.controllers.requestsAndResponses.UpdateRequest;
import com.semicolon.goodreadsbytope.data.models.User;
import com.semicolon.goodreadsbytope.data.models.VerificationMessageRequest;
import com.semicolon.goodreadsbytope.data.repositories.UserRepository;
import com.semicolon.goodreadsbytope.dtos.UserDto;
import com.semicolon.goodreadsbytope.events.SendMessageEvent;
import com.semicolon.goodreadsbytope.exceptions.GoodReadsException;
import com.semicolon.goodreadsbytope.security.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ModelMapper modelMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final TokenProvider tokenProvider;

    public UserServiceImpl(UserRepository userRepository,
                           ModelMapper mapper,
                           ApplicationEventPublisher applicationEventPublisher,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.modelMapper = mapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public UserDto createUserAccount(String host, AccountCreationRequest accountCreationRequest) throws GoodReadsException{
        validate(accountCreationRequest, userRepository);
        User user = new User(accountCreationRequest.getFirstName(), accountCreationRequest.getLastName(),
                accountCreationRequest.getEmail(), bCryptPasswordEncoder.encode(accountCreationRequest.getPassword()));
        user.setDateJoined(LocalDate.now());
        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateTokenForVerification(String.valueOf(savedUser.getId()));
        VerificationMessageRequest message = VerificationMessageRequest.builder()
                .subject("VERIFY EMAIL")
                .sender("ehizman.tutoredafrica@gmail.com")
                .receiver(user.getEmail())
                .domainUrl(host)
                .verificationToken(token)
                .usersFullName(String.format("%s %s", savedUser.getFirstName(), savedUser.getLastName()))
                .build();
        SendMessageEvent event = new SendMessageEvent(message);
        applicationEventPublisher.publishEvent(event);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto  findUserById(String userId) throws GoodReadsException {
        User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(
                () -> new GoodReadsException(String.format("User with id %s not found", userId), 404)
        );
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class)).toList();
    }

    @Override
    public UserDto updateUserProfile(String id, UpdateRequest updateRequest) throws GoodReadsException {
        User user = userRepository.findById(Long.valueOf(id)).orElseThrow(
                () -> new GoodReadsException("user id not found", 404)
        );
        User userToSave = modelMapper.map(updateRequest,User.class);
        userToSave.setId(user.getId());
        userToSave.setPassword(user.getPassword());
        userToSave.setRoles(user.getRoles());
        userToSave.setDateJoined(user.getDateJoined());
        userToSave.setVerified(user.isVerified());
        userRepository.save(userToSave);
        return modelMapper.map(userToSave, UserDto.class);
    }

    @Override
    public User findUserByEmail(String email) throws GoodReadsException {
        return userRepository.findUserByEmail(email).orElseThrow(()-> new GoodReadsException("user not found", 400));
    }

    @Override
    public void verifyUser(String token) throws GoodReadsException {
        Claims claims = tokenProvider.getAllClaimsFromJWTToken(token);
        Function<Claims, String> getSubjectFromClaim = Claims::getSubject;
        Function<Claims, Date> getExpirationDateFromClaim = Claims::getExpiration;
        Function<Claims, Date> getIssuedAtDateFromClaim = Claims::getIssuedAt;

        String userId = getSubjectFromClaim.apply(claims);
        if (userId == null){
            throw new GoodReadsException("User id not present in verification token", 404);
        }
        Date expiryDate = getExpirationDateFromClaim.apply(claims);
        if (expiryDate == null){
            throw new GoodReadsException("Expiry Date not present in verification token", 404);
        }
        Date issuedAtDate = getIssuedAtDateFromClaim.apply(claims);

        if (issuedAtDate == null){
            throw new GoodReadsException("Issued At date not present in verification token", 404);
        }

        if (expiryDate.compareTo(issuedAtDate) > 14.4 ){
            throw new GoodReadsException("Verification Token has already expired", 404);
        }

        User user = findUserByIdInternal(userId);
        if (user == null){
            throw new GoodReadsException("User id does not exist",404);
        }
        user.setVerified(true);
        userRepository.save(user);
    }

    private User findUserByIdInternal(String userId) {
        return userRepository.findById(Long.valueOf(userId)).orElse(null);
    }

    private static void validate(AccountCreationRequest accountCreationRequest, UserRepository userRepository) throws GoodReadsException {

        User user = userRepository.findUserByEmail(accountCreationRequest.getEmail()).orElse(null);
        if (user != null){
            throw new GoodReadsException("user email already exists", 400);
        }
    }

    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user != null){
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user.getRoles()));
        }
        return null;
    }
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream().map(
                role -> new SimpleGrantedAuthority(role.getRoleType().name())
        ).collect(Collectors.toSet());
    }
}


