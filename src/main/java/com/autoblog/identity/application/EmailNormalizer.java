package com.autoblog.identity.application;

import org.springframework.stereotype.Service;

@Service
public class EmailNormalizer {

    public String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
