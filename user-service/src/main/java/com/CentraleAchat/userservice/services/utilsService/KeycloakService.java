package com.CentraleAchat.userservice.services.utilsService;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.Token;
import org.keycloak.TokenVerifier;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.account.SessionRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    public static AccessTokenResponse authenticate(String username, String password) {

        Keycloak keycloak = KeycloakBuilder
                .builder()
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .clientId("pidev")
                .clientSecret("pvs3oudngIxtojOSiITvfcU2QTn7cgkr")
                .serverUrl("http://localhost:8099/")
                .realm("pidev")
                .build();

        RealmResource realmResource = keycloak.realm("pidev");

        return keycloak.tokenManager().getAccessToken();
    }
    public static void logout(String accessToken) throws IOException {
        String keycloakUrl="http://localhost:8099/";
        String realm="pidev";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout");
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("client_id", "your_client_id"));
        formParams.add(new BasicNameValuePair("refresh_token", accessToken));
        UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");
        httpPost.setEntity(form);
        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 204) {
            throw new RuntimeException("Failed to logout: " + statusCode);
        }
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
public void test(){
    List<UserRepresentation> users = keycloak().realm("pidev").users().list();
    for (UserRepresentation user : users) {
        List<UserSessionRepresentation> sessions = keycloak().realm("pidev").users().get(user.getId()).getUserSessions();
        for (UserSessionRepresentation session : sessions) {
            int sessionLength = (int) Duration.between(Instant.ofEpochSecond(session.getStart()), Instant.ofEpochSecond(session.getLastAccess())).getSeconds();
            System.out.println("User: " + user.getUsername() + ", Session length: " + ((float)sessionLength/1000)/60 + " minutes");
        }
    }
}

}

