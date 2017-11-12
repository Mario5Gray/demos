package com.santas.cap.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CapClient {

    @Autowired
    RestTemplate restTemplate;

    @Value("${capone.app.url}")
    String url;

    @Value("${capone.auth.url}")
    String authUrl;

    @Value("${capone.client.id}")
    String clientId;

    @Value("${capone.client.secret}")
    String clientSecret;

    public CapAuth accessToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(
                        new LinkedMultiValueMap<String, String>() {{
                            add("client_id", clientId);
                            add("client_secret", clientSecret);
                            add("grant_type", "client_credentials");
                        }},
                        headers
                );

        return restTemplate.postForEntity(authUrl,
                request,
                CapAuth.class)
                .getBody();
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
class CapAuth {
    String issuedAt;
    String tokenType;
    Long expiresIn;
    String accessToken;
}