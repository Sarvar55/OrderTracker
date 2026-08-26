package com.codems.ordertracker.common.security.model;

import com.codems.ordertracker.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public final class SecurityUser implements UserDetails, CredentialsContainer {

    private final Long userId;
    private final String displayName;
    private final String email;
    private String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> authorities;

    private SecurityUser(User user) {
        this.userId = user.getId();
        this.displayName = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
        this.authorities = List.of();
    }

    public static SecurityUser from(User user) {
        return new SecurityUser(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }
}
