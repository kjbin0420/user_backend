package com.dsmpear.main.domain.auth.entity.refreshtoken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor @AllArgsConstructor @Getter
@Builder
public class RefreshToken {
    @Id
    private String email;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Long refreshExp;

    public RefreshToken update(String refreshToken, Long refreshExp) {
        this.refreshToken = refreshToken;
        this.refreshExp = refreshExp;
        return this;
    }
}
