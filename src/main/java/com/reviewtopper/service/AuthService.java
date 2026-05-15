package com.reviewtopper.service;

import com.reviewtopper.config.ReviewTopperProperties;
import com.reviewtopper.dto.auth.ChangePasswordRequest;
import com.reviewtopper.dto.auth.ForgotPasswordRequest;
import com.reviewtopper.dto.auth.ForgotPasswordResponse;
import com.reviewtopper.dto.auth.JwtResponse;
import com.reviewtopper.dto.auth.LoginRequest;
import com.reviewtopper.dto.auth.RegisterRequest;
import com.reviewtopper.dto.auth.ResetPasswordRequest;
import com.reviewtopper.dto.auth.UserProfileResponse;
import com.reviewtopper.entity.PasswordResetToken;
import com.reviewtopper.entity.User;
import com.reviewtopper.enums.UserRole;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.exception.ConflictException;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.repository.PasswordResetTokenRepository;
import com.reviewtopper.repository.UserRepository;
import com.reviewtopper.security.JwtTokenProvider;
import com.reviewtopper.util.SecureTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionService subscriptionService;
    private final ReviewTopperProperties properties;

    @Transactional
    public JwtResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("Email already registered.");
        }
        User user = User.builder()
                .name(req.name())
                .email(req.email().trim().toLowerCase())
                .phone(req.phone())
                .password(passwordEncoder.encode(req.password()))
                .verified(true)
                .role(UserRole.OWNER)
                .build();
        userRepository.save(user);
        subscriptionService.assignStarterSubscription(user);

        String token = jwtTokenProvider.generate(user);
        return new JwtResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        User user = userRepository
                .findByEmailIgnoreCase(req.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials."));
        String token = jwtTokenProvider.generate(user);
        return new JwtResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        String generic =
                "If an account exists for this email address, password reset instructions have been processed.";
        if (user == null) {
            return ForgotPasswordResponse.genericMessage(generic);
        }
        passwordResetTokenRepository.deleteByUser_Id(user.getId());

        byte[] raw = new byte[48];
        RANDOM.nextBytes(raw);
        String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hash = SecureTokenHasher.sha256Hex(plainToken);

        PasswordResetToken entity = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(entity);

        if (properties.getAuth().isExposeResetTokenInResponse()) {
            return ForgotPasswordResponse.withDevToken(generic + " (development flag enabled)", plainToken);
        }
        return ForgotPasswordResponse.genericMessage(generic);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String hash = SecureTokenHasher.sha256Hex(req.resetToken());
        PasswordResetToken token =
                passwordResetTokenRepository.findByTokenHash(hash).orElseThrow(() -> new NotFoundException("Invalid reset token."));
        if (!token.isUsable()) {
            throw new BadRequestException("Reset token expired or already used.");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        token.setUsedAt(Instant.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isVerified());
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}
