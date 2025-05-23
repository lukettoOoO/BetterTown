package com.mycompany.bettertown;

import java.util.Date;

public class IssueData
{
    private String title;
    private String description;
    private String photo;
    private int priority;
    private String city;
    private String address;
    private Date date;
    private String username;
    private String status;
    
    private double latitude;
    private double longitude;

    
    public IssueData(String title, String description, String photo, int priority, String city, String address, Date date, String username, String status, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.photo = photo;
        this.priority = priority;
        this.city = city;
        this.address = address;
        this.date = date;
        this.username = username;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public IssueData()
    {
        //folosit pentru crearea unui obiect in database
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }

    public int getPriority() {
        return priority;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public Date getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }  
    
}
