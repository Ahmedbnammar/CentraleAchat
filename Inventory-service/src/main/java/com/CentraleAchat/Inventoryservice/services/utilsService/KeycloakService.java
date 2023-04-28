package com.CentraleAchat.Inventoryservice.services.utilsService;

import org.keycloak.TokenVerifier;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Configuration
public class KeycloakService {

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8099/")
                .realm("pidev")
                .clientId("pidev")
                .clientSecret("pvs3oudngIxtojOSiITvfcU2QTn7cgkr")
                .username("systemadmin@mail.com")
                .password("systemadmin")
                .build();
    }

    //    public  AccessToken getAccessToken() {
//        KeycloakSecurityContext context = (KeycloakSecurityContext) SecurityContextHolder.getContext().getAuthentication().getCredentials();
//        return context.getToken();
//    }
    public String getAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public AccessToken whoAmI() {
        KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        AccessToken accessToken = authentication.getAccount().getKeycloakSecurityContext().getToken();
        return accessToken;
    }

    public boolean verifyToken(String tokenString) {
        AccessToken token = null;
        try {
            System.out.println(tokenString);
            token = TokenVerifier.create(tokenString, AccessToken.class).getToken();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("error");
            return false;
        }
        if (token == null) {
            System.out.println("null");
            return false;
        }
        if (!token.isActive()) {
            System.out.println("not active");
            return false;
        }
        return true;
    }

    public void test() {
        List<UserRepresentation> users = keycloak().realm("pidev").users().list();
        for (UserRepresentation user : users) {
            List<UserSessionRepresentation> sessions = keycloak().realm("pidev").users().get(user.getId()).getUserSessions();
            for (UserSessionRepresentation session : sessions) {
                int sessionLength = (int) Duration.between(Instant.ofEpochSecond(session.getStart()), Instant.ofEpochSecond(session.getLastAccess())).getSeconds();
                System.out.println("User: " + user.getUsername() + ", Session length: " + ((float) sessionLength / 1000) / 60 + " minutes");
            }
        }
    }

}

