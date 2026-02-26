package io.github.jvlealc.securecapita.domain;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "two_factor_verifications")
public class MfaVerification implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(length = 8, unique = true, nullable = false)
    private String code;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    public MfaVerification() {}

    public MfaVerification(User user, String code, LocalDateTime expirationDate) {
        this.user = user;
        this.code = code;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MfaVerification that = (MfaVerification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MfaVerification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", code='" + "***" + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
