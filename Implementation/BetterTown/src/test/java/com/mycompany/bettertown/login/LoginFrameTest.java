package com.mycompany.bettertown.login;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.lang.reflect.Field;
import java.awt.event.ActionEvent;

public class LoginFrameTest {

    private LoginFrame instance;
    //swing fields that we are going to use through reflection
    private JTextField emailTextField;
    private JPasswordField passwordTextField;
    private JRadioButton userRadioButton;
    private JRadioButton adminRadioButton;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginFrameTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before //before every test
    public void setUp() throws Exception {

        instance = new LoginFrame();

        emailTextField = getPrivateField(instance, "emailTextField", JTextField.class);
        passwordTextField = getPrivateField(instance, "passwordTextField", JPasswordField.class);
        userRadioButton = getPrivateField(instance, "userRadioButton", JRadioButton.class);
        adminRadioButton = getPrivateField(instance, "adminRadioButton", JRadioButton.class);
        loginButton = getPrivateField(instance, "loginButton", JButton.class);
        errorLabel = getPrivateField(instance, "errorLabel", JLabel.class);
    }

    @After //after every test
    public void tearDown() {

        if (instance != null) {
           
        }
    }

    //accessing private fields through refection
    private <T> T getPrivateField(Object obj, String fieldName, Class<T> fieldType) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return fieldType.cast(field.get(obj));
    }

    //set text fields but don't select any roles test
    @Test
    public void testLoginButtonActionPerformed_NoRoleSelected() {
        System.out.println("testLoginButtonActionPerformed_NoRoleSelected");
        
        emailTextField.setText("test@example.com");
        passwordTextField.setText("password123");

        //simulate login button click
        loginButton.doClick();

        assertEquals("error label should show 'Please select a role!'",
                     "Please select a role!", errorLabel.getText());
    }

    //email field is empty test
    @Test
    public void testLoginButtonActionPerformed_EmptyEmail() {
        System.out.println("testLoginButtonActionPerformed_EmptyEmail");

        //select a role, but leave email empty
        userRadioButton.setSelected(true);
        userRadioButton.getActionListeners()[0].actionPerformed(new ActionEvent(userRadioButton, ActionEvent.ACTION_PERFORMED, ""));
        passwordTextField.setText("password123");
        emailTextField.setText("");

        loginButton.doClick();

        assertEquals("error label should show 'Please enter an email!'",
                     "Please enter an email!", errorLabel.getText());
    }

    //empty password field test
    @Test
    public void testLoginButtonActionPerformed_EmptyPassword() {
        System.out.println("testLoginButtonActionPerformed_EmptyPassword");

        //select a role and enter an email, but leave password empty
        userRadioButton.setSelected(true);
        userRadioButton.getActionListeners()[0].actionPerformed(new ActionEvent(userRadioButton, ActionEvent.ACTION_PERFORMED, ""));
        emailTextField.setText("test@example.com");
        passwordTextField.setText(""); // Parola goală

        loginButton.doClick();

        //verify error label
        assertEquals("error label should show 'Please enter a password!'",
                     "Please enter a password!", errorLabel.getText());
    }
    
    //integration test for database validation
    @Test
    public void testLoginButtonActionPerformed_ValidInputsNoDatabaseMock() {
        System.out.println("testLoginButtonActionPerformed_ValidInputsNoDatabaseMock");

        userRadioButton.setSelected(true);
        userRadioButton.getActionListeners()[0].actionPerformed(new ActionEvent(userRadioButton, ActionEvent.ACTION_PERFORMED, ""));
        emailTextField.setText("nonexistent@example.com"); //an email that is not in the database
        passwordTextField.setText("somepassword");

        loginButton.doClick();

        String errorMessage = errorLabel.getText();
        assertTrue("error label should show a database-related error for non-existent user or wrong password",
                   errorMessage.contains("User not found") || errorMessage.contains("Incorrect password!") || errorMessage.contains("Database error"));
    }

    @Test
    public void testMain() {
        System.out.println("main");
    }
}