package com.jung.planet.security.UserDetail;


import com.jung.planet.user.entity.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final UserRole userRole;
    private final String password = "";

    private Collection<? extends GrantedAuthority> authorities;


    public CustomUserDetails(Long userId, String username, UserRole userRole, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
        this.authorities = authorities;
    }

    // UserDetails 인터페이스의 메서드들을 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
