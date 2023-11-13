package com.jung.planet.user.service;

import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    void testProcessUser_NewUser() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setName("New User");
        userDTO.setEmail("newuser@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).thenReturn("mockRefreshToken");

        // When
        User result = userService.processUser(userDTO);

        // Then
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("New User", result.getName());
        assertEquals("mockRefreshToken", result.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testProcessUser_ExistingUser() {
        // Given
        User existingUser = User.builder().email("existinguser@example.com").name("Existing User").build();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("existinguser@example.com");
        userDTO.setName("Existing User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).thenReturn("mockRefreshToken");

        // When
        User result = userService.processUser(userDTO);

        // Then
        assertEquals("existinguser@example.com", result.getEmail());
        assertEquals("Existing User", result.getName());
        assertEquals("mockRefreshToken", result.getRefreshToken());
    }
}
