package io.github.joaovitorleal.securecapita.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uq_users_email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 40, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 40, nullable = false)
    private String lastName;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column
    private String password;

    @Column(length = 30)
    private String phone;

    @Column
    private String address;

    @Column(length = 50)
    private String title;

    @Column(length = 500)
    private String bio;

    @Builder.Default
    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean enabled = false;

    @Builder.Default
    @Column(name = "non_locked", columnDefinition = "boolean default true", nullable = false)
    private boolean nonLocked = true;

    @Builder.Default
    @Column(name = "using_mfa", columnDefinition = "boolean default false", nullable = false)
    private boolean usingMfa = false;

    @Column(name = "image_url")
    private String imageUrl = "https://cdn-icons-png.flaticon.com/512/3033/3033143.png";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Role role;
}

