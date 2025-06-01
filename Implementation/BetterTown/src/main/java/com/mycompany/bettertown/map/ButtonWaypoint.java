/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown.map;

import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author luca
 */
public class ButtonWaypoint extends JButton {
    
    public ButtonWaypoint()
    {
        setContentAreaFilled(false);
        setIcon(new ImageIcon(getClass().getResource("/pinUnresolved.png")));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setSize(new Dimension(24, 24));
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }
}
