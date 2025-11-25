package com.onion.onionserver.model.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "user_auth")
public class UserAuth {
    @Id
    private Integer id;

    @Column(name = "hash", nullable = false, length = 256)
    private String hash;

    @Column(name = "salt", nullable = false, length = 32)
    private String salt;

    @OneToOne(cascade = CascadeType.DETACH)
    @MapsId
    @JoinColumn(name = "id")
    private User user;
}
