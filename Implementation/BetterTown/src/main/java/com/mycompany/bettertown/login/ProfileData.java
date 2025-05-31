package com.mycompany.bettertown.login;

//this class contains variables for the registered accounts
public class ProfileData
{
    //Am adaugat campul de ig plus un getter si un setter pe care i-am folost prin metode din baza de date
    private int id;
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
    public ProfileData(int id,String name, String city, String password, String email, String status) {
        this.id=id;
        this.name = name;
        this.city = city;
        this.password = password;
        this.email = email;
        this.status = status;
    }
    public ProfileData()
    {
    
    }
    public int getId()
    {
    return id;
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
    
    public void setId(int id){
        this.id=id;
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