package com.jung.planet.user.service;

import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public User processUser(UserDTO userDTO) {

        Optional<User> user = userRepository.findByEmail(userDTO.toEntity().getEmail());

        // 사용자가 존재하지 않으면 새로운 사용자를 생성
        if (user.isEmpty()) {
            User newUser = User.builder().email(userDTO.getEmail()).name(userDTO.getName()).build();
            String refreshToken = jwtTokenProvider.createRefreshToken(newUser.getId(), newUser.getEmail());
            newUser.setRefreshToken(refreshToken);

            userRepository.save(newUser);
            return newUser;
        } else {
            User existedUer = user.get();
            String refreshToken = jwtTokenProvider.createRefreshToken(existedUer.getId(), existedUer.getEmail());
            existedUer.setRefreshToken(refreshToken);
            return existedUer;
        }
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        userRepository.deleteById(userId);
    }


    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
             u.setRefreshToken(refreshToken);
            userRepository.save(u);
        });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
