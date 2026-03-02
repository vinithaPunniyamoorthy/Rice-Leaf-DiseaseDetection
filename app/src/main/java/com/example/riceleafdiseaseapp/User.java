package com.example.riceleafdiseaseapp;

public class User {
    private String id;
    private String username;
    private String email;
    private String region;

    public User(String id, String username, String email, String region) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.region = region;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRegion() { return region; }
}