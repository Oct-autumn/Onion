package com.onion.onionserver.model.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name="project_member")
public class ProjectMember {
    @Embeddable
    public static class ProjectMemberPK implements Serializable {
        private Long user_id;
        private Long project_id;
    }

    @EmbeddedId
    private ProjectMemberPK id;

    @Column(name = "role", nullable = false)
    private int role;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne
    @MapsId("project_id")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @MapsId("user_id")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
