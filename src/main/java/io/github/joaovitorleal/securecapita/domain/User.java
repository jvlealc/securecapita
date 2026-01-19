package io.github.joaovitorleal.securecapita.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 3L;

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

    @Builder.Default
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", title='" + title + '\'' +
                ", bio='" + bio + '\'' +
                ", enabled=" + enabled +
                ", nonLocked=" + nonLocked +
                ", usingMfa=" + usingMfa +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", roleId=" + (role != null ? role.getId() : null) +
                ", roleName=" + (role != null ? role.getName() : null) +
                '}';
    }
}

