package com.seplag.music.service;

import com.seplag.music.domain.dto.*;
import com.seplag.music.domain.model.User;
import com.seplag.music.domain.model.UserRole;
import com.seplag.music.repository.UserRepository;
import com.seplag.music.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Tentando login para usuário: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Senha inválida para usuário: {}", request.getUsername());
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        log.info("Login bem-sucedido para usuário: {}", request.getUsername());

        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpirationTimeMs() / 1000)
                .user(userMapper.toDTO(user))
                .build();
    }

    public LoginResponseDTO register(RegisterRequestDTO request) {
        log.info("Tentando registrar usuário: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username já existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já existe");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", savedUser.getUsername());

        String accessToken = jwtProvider.generateAccessToken(savedUser.getUsername(), savedUser.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpirationTimeMs() / 1000)
                .user(userMapper.toDTO(savedUser))
                .build();
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String token = request.getRefreshToken();

        if (!jwtProvider.validateToken(token)) {
            throw new RuntimeException("Refresh token inválido ou expirado");
        }

        String username = jwtProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String newAccessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpirationTimeMs() / 1000)
                .user(userMapper.toDTO(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return userMapper.toDTO(user);
    }
}