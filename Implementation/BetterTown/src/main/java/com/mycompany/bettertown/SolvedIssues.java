/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown;

/**
 *
 * @author biancagrovu
 */
public class SolvedIssues {
    private int id;
    private int userId;
    private int issueId;
    public SolvedIssues()
    {
    
    }
    public SolvedIssues(int id,int userId,int issueId)
    {
        this.id=id;
        this.userId=userId;
        this.issueId=issueId;
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
}
