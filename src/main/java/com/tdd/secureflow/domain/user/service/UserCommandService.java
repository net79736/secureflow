package com.tdd.secureflow.domain.user.service;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserCommand.CreateUserCommand;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam.CreateUserParam;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    public User createBasicUser(CreateUserCommand command) {
        return userRepository.createUser(
                new CreateUserParam(
                        command.email(),
                        command.password(),
                        command.name(),
                        USER,
                        LOCAL
                )
        );
    }


}
