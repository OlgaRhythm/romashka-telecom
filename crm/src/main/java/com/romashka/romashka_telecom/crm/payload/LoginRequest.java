package com.romashka.romashka_telecom.crm.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
} 