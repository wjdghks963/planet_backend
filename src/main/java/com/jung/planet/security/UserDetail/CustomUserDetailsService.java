package com.jung.planet.security.UserDetail;

import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        Logger logger =  LoggerFactory.getLogger(this.getClass());
        logger.debug("USER :: {}",user);

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                Collections.emptyList()
        );
    }
}
