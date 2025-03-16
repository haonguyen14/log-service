package com.haonguyen.logService.authentication;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfig {
    @Value("${authentication.jwt.publicKey}")
    @Getter
    private String jwtPublicKey;
}
