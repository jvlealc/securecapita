package io.github.joaovitorleal.securecapita.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_verifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorVerification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(length = 10, unique = true, nullable = false)
    private String code;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
}
