package com.onion.onionserver.model.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "role", nullable = false)
    private int role; // 0: normal user, 1: admin

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserAuth userAuth;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == 1) {
            return java.util.Collections.singletonList(new Authority("ROLE_ADMIN"));
        } else {
            return java.util.Collections.singletonList(new Authority("ROLE_USER"));
        }
    }

    public static class Authority implements GrantedAuthority {
        private final String authority;

        public Authority(String authority) {
            this.authority = authority;
        }

        @Override
        public String getAuthority() {
            return authority;
        }
    }
}

