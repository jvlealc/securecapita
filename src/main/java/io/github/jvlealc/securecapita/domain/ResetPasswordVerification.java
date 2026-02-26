package io.github.jvlealc.securecapita.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reset_password_verifications")
public class ResetPasswordVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String url;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    // Construtor padrão
    public ResetPasswordVerification() {
    }

    // Construtor de conveniência
    public ResetPasswordVerification(User user, String url, LocalDateTime expirationDate) {
        this.user = user;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        ResetPasswordVerification that = (ResetPasswordVerification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ResetPasswordVerification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", url='" + "[PROTECTED]" + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
