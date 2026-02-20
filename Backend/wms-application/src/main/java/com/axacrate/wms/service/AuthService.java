package com.axacrate.wms.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.axacrate.wms.dto.JwtResponseDTO;
import com.axacrate.wms.dto.LoginRequestDTO;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public JwtResponseDTO login(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Because AppUser implements UserDetails, principal will be an AppUser
        AppUser user = (AppUser) auth.getPrincipal();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        String token = jwtUtil.generateToken(extraClaims, user);
        return new JwtResponseDTO(token, user.getUsername(), user.getRole().name());
    }
}
