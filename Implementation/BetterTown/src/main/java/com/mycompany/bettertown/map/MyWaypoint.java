/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown.map;

import com.mycompany.bettertown.IssueData;
import com.mycompany.bettertown.user.MainTabsUser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author luca
 */
public class MyWaypoint extends DefaultWaypoint {
    
    private IssueData data;
    private JButton button;

    public MyWaypoint(IssueData data, EventWaypoint event, GeoPosition coord) {
        super(coord);
        this.data = data;
        initButton(event);
    }
    
    public JButton getButton() {
        return button;
    }

    public void setButton(JButton button) {
        this.button = button;
    }

    public IssueData getData() {
        return data;
    }

    public void setData(IssueData data) {
        this.data = data;
    }
    
    private void initButton(EventWaypoint event)
    {
        button = new ButtonWaypoint();
        button.addActionListener(new ActionListener() 
        {
           @Override
           public void actionPerformed(ActionEvent ae)
           {
               event.selected(MyWaypoint.this);
           }
        });
    }
    
}
