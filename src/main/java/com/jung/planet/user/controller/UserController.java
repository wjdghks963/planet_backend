package com.jung.planet.user.controller;

import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody UserDTO userDTO) {
        logger.info("Attempting to add a new user with email: {}", userDTO.getEmail());
        try {
            User newUser = userService.addUser(userDTO.toEntity());
            logger.info("New user added with id: {}", newUser.getId());
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            logger.error("Error adding user: {}", e.getMessage(), e);
            // 적절한 예외 처리를 수행하고, 클라이언트에게 에러 응답을 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
