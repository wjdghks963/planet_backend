package com.jung.planet.user.service;

import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    @Transactional
    public User processUser(UserDTO userDTO) {

        Optional<User> user = userRepository.findByEmail(userDTO.toEntity().getEmail());

        // 사용자가 존재하지 않으면 새로운 사용자를 생성
        if (user.isEmpty()) {
            User newUser = User.builder().email(userDTO.getEmail()).name(userDTO.getName()).build();

            userRepository.save(newUser);
            return newUser;
        } else {
            return user.get();
        }
    }

}
