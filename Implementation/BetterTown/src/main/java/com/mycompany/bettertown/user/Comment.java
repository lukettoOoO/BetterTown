/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown.user;

import java.util.Date;

/**
 *
 * @author biancagrovu
 */
public class Comment {

    private int id;
    private int issueId;
    private String title;
    private int userId;
    private Date date;
    private String content;
    //Nu stiu exact care din constructori o sa-i folosesc si de aia sunt doi
    public Comment(String title, Date date, String content)
    {
     this.title=title;
     this.date=date;
     this.content=content;
    }
    public Comment()
    {
    
    }
    public Comment(int id,int issueId, String title,int userId, Date date, String content)
    {
     this.id=id;
     this.issueId=issueId;
     this.title=title;
     this.userId=userId;
     this.date=date;
     this.content=content;
    }

    public int getId()
    {
        return id;
    }
    public int getIssueId()
    {
        return issueId;
    }
    public String getTitle()
    {
        return title;
    }
    public int getUserId()
    {
        return userId;
    }
    public Date getDate()
    {
        return date;
    }
    public String getContent()
    {
        return content;
    }
    public void setId(int id)
    {
        this.id=id;
    }
    public void setIssueId(int issueId)
    {
        this.issueId=issueId;
    }
    public void setTitle(String title)
    {
        this.title=title;
    }
    public void setUserId(int userId)
    {
        this.userId=userId;
    }
    public void setDate(Date date)
    {
        this.date=date;
    }
    public void setContent(String content)
    {
        this.content=content;
    }
    
}
