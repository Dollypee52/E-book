package com.semicolon.goodreadsbytope.config;

import com.semicolon.goodreadsbytope.data.models.User;
import com.semicolon.goodreadsbytope.data.models.enums.RoleType;
import com.semicolon.goodreadsbytope.data.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (userRepository.findUserByEmail("adminuser@gmail.com").isEmpty()){
            User user = new User("Admin", "User","adminuser@gmail.com", passwordEncoder.encode("password1234#"), RoleType.ROLE_ADMIN);
            user.setDateJoined(LocalDate.now());
            userRepository.save(user);
        }
    }
}

