/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown.user;

/**
 *
 * @author biancagrovu
 */
public class Alerts {
    private int id;
    private int userId;
    private String text;
    
    public Alerts(int id,int userId,String text)
    {
        this.id=id;
        this.userId=userId;
        this.text=text;
    }
    public Alerts()
    {
    
    }
    
    public int getId()
    {
        return id;
    }
    public int getUserId()
    {
        return userId;
    }
    public String getText()
    {
        return text;
    }
    public void setId(int id)
    {
        this.id=id;
    }
    public void setUserId(int userId)
    {
        this.userId=userId;
    }
    public void setText(String text)
    {
        this.text=text;
    }
}
