/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown;

/**
 *
 * @author biancagrovu
 */
public class Feedback {
    private int id;
    private int solvedId;
    private int userId;
    private String subject;
    private int rating;
    private String description;
    
    public Feedback()
    {
    
    }
    
    public int getId()
    {
        return id;
    }
    public int getSolvedId()
    {
        return solvedId;
    }
    public int getUserId()
    {
        return userId;
    }
    public String getSubject()
    {
        return subject;
    }
    public int getRating()
    {
        return rating;
    }
    public String getDescription()
    {
        return description;
    }
    public void setId(int id)
    {
        this.id=id;
    }
    public void setSolvedId(int solvedId)
    {
        this.solvedId=solvedId;
    }
    public void setUserId(int userId)
    {
        this.userId=userId;
    }
    public void setSubject(String subject)
    {
        this.subject=subject;
    }
    public void setRating(int rating)
    {
        this.rating=rating;
    }
    public void setDescription(String description)
    {
        this.description=description;
    }
}
