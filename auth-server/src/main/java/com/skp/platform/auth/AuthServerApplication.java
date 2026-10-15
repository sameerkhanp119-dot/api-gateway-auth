package com.skp.platform.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OAuth2 authorization server issuing signed JWT access tokens (client-credentials grant).
 * Clients, scopes and token lifetimes are declared in application.yml; signing keys are
 * exposed at /oauth2/jwks for resource servers such as the gateway.
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
