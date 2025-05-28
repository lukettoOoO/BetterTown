/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author biancagrovu
 */
public class SolvedIssues {
    private int id;
    private int userId;
    private int issueId;
    private LocalDateTime date;
    public SolvedIssues()
    {
    
    }
    public SolvedIssues(int id,int userId,int issueId,LocalDateTime date)
    {
        this.id=id;
        this.userId=userId;
        this.issueId=issueId;
        this.date=date;
    }
    public int getId()
    {
        return id;
    }
    public int getUserId()
    {
        return userId;
    }
    public int getIssueId()
    {
        return issueId;
    }
    public LocalDateTime getDate()
    {
        return date;
    }
    
    public void setId(int id)
    {
        this.id=id;
    }
    public void setUserId(int userId)
    {
        this.userId=userId;
    }
    public void setIssueId(int issueId)
    {
        this.issueId=issueId;
    }
    public void setDate(LocalDateTime date)
    {
        this.date=date;
    }
}
