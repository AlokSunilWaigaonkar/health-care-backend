package com.example.UserManagement.config;

import com.example.UserManagement.model.Users.Doctor;
import jakarta.persistence.Entity;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.print.Doc;
import java.util.Collection;
import java.util.List;

@Data
public class DoctorUserDetails implements UserDetails {

    private String userName;
    private String password;
    private boolean isEnabled;
    private List<GrantedAuthority> authorities;

    public DoctorUserDetails (Doctor doctor){
        this.userName = doctor.getSystemEmail();
        this.password = doctor.getSystemPassword();
        this.isEnabled = true;
        this.authorities = List.of(new SimpleGrantedAuthority("DOCTOR"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
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
        return isEnabled;
    }
}
