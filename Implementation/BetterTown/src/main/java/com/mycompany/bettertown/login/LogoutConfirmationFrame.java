package com.mycompany.bettertown.login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogoutConfirmationFrame extends JFrame {

    private LogoutListener logoutListener;

    public LogoutConfirmationFrame(LogoutListener listener) {
        this.logoutListener = listener;
        setTitle("Logout Confirmation");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));

        JLabel messageLabel = new JLabel("Are you sure you want to log out?");
        messageLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton yesButton = new JButton("Yes");
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (logoutListener != null) {
                    logoutListener.onLogoutConfirmed();
                }
                LoginFrame loginObj = new LoginFrame();
                loginObj.show();
                dispose();
            }
        });

        JButton noButton = new JButton("No");
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        mainPanel.add(new JPanel());
        mainPanel.add(messageLabel);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LogoutConfirmationFrame frame = new LogoutConfirmationFrame(null);
            frame.setVisible(true);
        });
    }
}