package com.haonguyen.logService.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class Authenticator {
    private final RSAPublicKey publicKey;
    private final JWTVerifier verifier;

    public Authenticator(@Autowired AuthenticationConfig config) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.publicKey = getPublicKey(config);
        this.verifier = JWT.require(Algorithm.RSA256(publicKey)).build();
    }

    public boolean isAuthenticated(String token) {
        try {
           verifier.verify(token);
           return true;
        } catch (Exception e) {
            return false;
        }
    }

    private RSAPublicKey getPublicKey(AuthenticationConfig config) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] decodedKey = Base64.getDecoder().decode(config.getJwtPublicKey());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
}
