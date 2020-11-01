package com.dsmpear.main.domain.auth.service;

import com.dsmpear.main.domain.auth.dto.response.AccessTokenResponse;
import com.dsmpear.main.domain.auth.entity.refreshtoken.RefreshToken;
import com.dsmpear.main.domain.auth.entity.refreshtoken.RefreshTokenRepository;
import com.dsmpear.main.domain.auth.dto.request.SignInRequest;
import com.dsmpear.main.domain.auth.dto.response.TokenResponse;
import com.dsmpear.main.domain.auth.exceptions.InvalidTokenException;
import com.dsmpear.main.domain.auth.exceptions.UserNotFoundException;
import com.dsmpear.main.domain.user.entity.User;
import com.dsmpear.main.domain.user.entity.UserRepository;
import com.dsmpear.main.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${auth.jwt.exp.refresh}")
    private Long refreshExp;

    @Override
    public TokenResponse signIn(SignInRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .map(User::getEmail)
                    .map(email -> {
                        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
                        return new RefreshToken(email, refreshToken, refreshExp);
                    })
                    .map(refreshTokenRepository::save)
                    .map(refreshToken -> {
                        String accessToken = jwtTokenProvider.generateAccessToken(refreshToken.getEmail());
                        return new TokenResponse(accessToken, refreshToken.getRefreshToken(), refreshToken.getRefreshExp());
                    })
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public AccessTokenResponse tokenRefresh(String receivedToken) {
        if (!jwtTokenProvider.isRefreshToken(receivedToken)) {
            throw new InvalidTokenException();
        }

        return refreshTokenRepository.findByRefreshToken(receivedToken)
                .map(refreshToken -> {
                    String generateRefreshToken = jwtTokenProvider.generateRefreshToken(refreshToken.getEmail());
                    return refreshToken.update(generateRefreshToken, refreshExp);
                })
                .map(refreshTokenRepository::save)
                .map(refreshToken -> new AccessTokenResponse(jwtTokenProvider.generateAccessToken(refreshToken.getEmail())))
                .orElseThrow(UserNotFoundException::new);
    }
}
