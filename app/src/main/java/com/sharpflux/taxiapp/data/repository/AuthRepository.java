package com.sharpflux.taxiapp.data.repository;

public class AuthRepository {

    public boolean login(String username, String password) {
        // Dummy logic (replace with API call using Volley later)
        return username.equals("admin") && password.equals("1234");
    }
}
