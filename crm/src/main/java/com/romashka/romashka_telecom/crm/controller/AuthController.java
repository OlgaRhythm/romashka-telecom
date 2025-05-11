package com.romashka.romashka_telecom.crm.controller;

import com.romashka.romashka_telecom.crm.model.ErrorResponse;
import com.romashka.romashka_telecom.crm.payload.LoginRequest;
import com.romashka.romashka_telecom.crm.payload.JwtResponse;
import com.romashka.romashka_telecom.crm.security.JwtTokenProvider;
import com.romashka.romashka_telecom.crm.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userPrincipal.getUsername(),
                    userPrincipal.getRole()));
        } catch (BadCredentialsException ex) {
            // неправильный логин/пароль
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(1001, "Ошибка аутентификации"));
        } catch (Exception ex) {
            // всё остальное — внутренняя ошибка
            log.error("Login failed", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(1003, "Внутренняя ошибка сервера"));
        }
    }
} 