package com.tertir.demo.persistance.user;

import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.security.user.ApplicationUserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name="users")
public class ApplicationUser implements UserDetails {

    @javax.persistence.Id
    @GeneratedValue
    private UUID Id;

    private String username;
    private String password;

    private String IDBankNumber;

    private String otherBankNumber;

    @Enumerated(EnumType.STRING)
    private ApplicationUserRole role;

    private Boolean locked = false;

    private Boolean enabled = false;

    @OneToOne
    private QrEntity currentQrEntity;

    @OneToOne
    private QrEntity previousQrEntity;

    private Double sum;

    private LocalDateTime registrationTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        return Collections.singleton(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {return username;}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {return enabled;}

}
