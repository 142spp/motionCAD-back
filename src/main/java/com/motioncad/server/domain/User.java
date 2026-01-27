package com.motioncad.server.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String provider; // LOCAL, GOOGLE, etc.

    @Column
    private String providerId; // OAuth2 provider's unique user ID

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_settings", columnDefinition = "jsonb")
    private UserSettings userSettings;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalProjects = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalLikes = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalViews = 0;

    @Column
    private String region;

    @Column
    private String job;

    @Column(columnDefinition = "TEXT")
    private String userDescription;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "favorite_parts", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "part_id"))
    private Set<Part> favoriteParts = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "project_likes", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<Project> likedProjects = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "follows", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "followee_id"))
    private Set<User> following = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();
}
