package com.bsourichanh.javaspring.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserValidationServiceImpl implements UserValidationService {

    private final RestClient userRestClient;

    public UserValidationServiceImpl(RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    @Override
    public boolean isValidUser(String userId) {
        if (userId == null || userId.isBlank()) return false;
        try {
            Boolean result = userRestClient.get()
                    .uri("/users/{id}/valid", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {})
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
