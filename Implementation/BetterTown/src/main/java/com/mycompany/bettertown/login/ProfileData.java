package com.mycompany.bettertown.login;

//this class contains variables for the registered accounts
public class ProfileData
{
    private String name;
    private String city;
    private String password;
    private String email;
    private String status;

    public ProfileData(String name, String city, String password, String email, String status) {
        this.name = name;
        this.city = city;
        this.password = password;
        this.email = email;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
}