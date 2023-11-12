package com.jung.planet.user.service;

import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private  UserService userService;

    @Test
    public void whenNewUser_thenCreateUser() {
        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("asdads@naver.com");
        userDTO.setName("user11");
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty()); // 이메일로 사용자를 찾을 때 빈 결과를 반환하도록 설정

        // when
        User result = userService.processUser(userDTO);

        // then
        verify(userRepository).save(any(User.class)); // userRepository의 save 메서드가 호출되었는지 검증
        assertEquals(userDTO.getEmail(), result.getEmail()); // 반환된 User 객체의 이메일이 요청한 DTO의 이메일과 일치하는지 검증
        assertEquals(userDTO.getName(), result.getName()); // 이름 검증
    }
    @Test
    public void whenExistingUser_thenFetchUser() {
        // given
        User existingUser = new User("user@example.com", "Test User");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("asdads@naver.com");
        userDTO.setName("user11");
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(existingUser)); // 이메일로 사용자를 찾았을 때 기존 사용자를 반환하도록 설정

        // when
        User result = userService.processUser(userDTO);

        // then
        verify(userRepository, Mockito.never()).save(any(User.class)); // userRepository의 save 메서드가 호출되지 않았는지 검증
        assertEquals(existingUser.getEmail(), result.getEmail()); // 반환된 User 객체가 기존 User 객체와 일치하는지 검증
        assertEquals(existingUser.getName(), result.getName()); // 이름 검증
    }

}