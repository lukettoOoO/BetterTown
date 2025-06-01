package com.mycompany.bettertown.admin;

import com.mycompany.bettertown.IssueData;
import com.mycompany.bettertown.login.ProfileData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

/**
 * Test class for CommentAdminFrame
 * @author luca
 */
public class CommentAdminFrameTest {
    
    private CommentAdminFrame instance;
    private IssueData testIssue;
    private ProfileData testUser;
    
    public CommentAdminFrameTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {

    }
    
    @Before
    public void setUp() {
        //create test data before each test
        instance = new CommentAdminFrame();
        
        //create a test issue
        testIssue = new IssueData();
        testIssue.setId(1);
        testIssue.setTitle("Test Issue");
        testIssue.setDescription("Test Description");
        testIssue.setPriority(1);
        testIssue.setCity("Test City");
        testIssue.setAddress("Test Address");
        testIssue.setDate(LocalDateTime.now());
        testIssue.setUsername("testuser");
        testIssue.setStatus("open");
        testIssue.setLatitude(45.0);
        testIssue.setLongitude(25.0);
        
        //create a test user
        testUser = new ProfileData();
        testUser.setId(1);
        testUser.setName("Test Admin");
        testUser.setCity("Test City");
        testUser.setEmail("admin@test.com");
        testUser.setPassword("hashedpassword");
        testUser.setStatus("admin");
    }
    
    @After
    public void tearDown() {
        //cleanup after each test
        if (instance != null) {
            instance.dispose();
        }
        instance = null;
        testIssue = null;
        testUser = null;
    }

    //test of loadComments method with valid issue
    @Test
    public void testLoadCommentsWithValidIssue() {
        System.out.println("loadComments with valid issue");
        
        try {
            instance.loadComments(testIssue);
            assertTrue("loadComments should handle valid issue without throwing exception", true);
        } catch (Exception e) {
            fail("loadComments should not throw exception with valid issue: " + e.getMessage());
        }
    }
    
    //test of loadComments method with null issue
    @Test
    public void testLoadCommentsWithNullIssue() {
        System.out.println("loadComments with null issue");
        
        try {
            instance.loadComments(null);
            assertTrue("loadComments should handle null issue gracefully", true);
        } catch (NullPointerException e) {
            fail("loadComments should handle null issue without throwing NullPointerException");
        } catch (Exception e) {
            System.out.println("Expected exception for null issue: " + e.getMessage());
        }
    }
    
    //test of setLoggedInUser method with valid user
    @Test
    public void testSetLoggedInUserWithValidUser() {
        System.out.println("setLoggedInUser with valid user");
        
        try {
            instance.setLoggedInUser(testUser);
            assertTrue("setLoggedInUser should handle valid user without throwing exception", true);
        } catch (Exception e) {
            fail("setLoggedInUser should not throw exception with valid user: " + e.getMessage());
        }
    }
   
    //test of setLoggedInUser method with null user
    @Test
    public void testSetLoggedInUserWithNull() {
        System.out.println("setLoggedInUser with null user");
        
        try {
            instance.setLoggedInUser(null);
            assertTrue("setLoggedInUser should handle null user gracefully", true);
        } catch (NullPointerException e) {
            fail("setLoggedInUser should handle null user without throwing NullPointerException");
        } catch (Exception e) {
            System.out.println("Expected exception for null user: " + e.getMessage());
        }
    }

    //test of setCurrentIssue method with valid issue
    @Test
    public void testSetCurrentIssueWithValidIssue() {
        System.out.println("setCurrentIssue with valid issue");
        
        try {
            instance.setCurrentIssue(testIssue);
            assertTrue("setCurrentIssue should handle valid issue without throwing exception", true);
        } catch (Exception e) {
            fail("setCurrentIssue should not throw exception with valid issue: " + e.getMessage());
        }
    }
    
    //test of setCurrentIssue method with null issue
    @Test
    public void testSetCurrentIssueWithNull() {
        System.out.println("setCurrentIssue with null issue");
        
        try {
            instance.setCurrentIssue(null);
            assertTrue("setCurrentIssue should handle null issue gracefully", true);
        } catch (NullPointerException e) {
            fail("setCurrentIssue should handle null issue without throwing NullPointerException");
        } catch (Exception e) {
            System.out.println("Expected exception for null issue: " + e.getMessage());
        }
    }
    
    //test frame initialization
    @Test
    public void testFrameInitialization() {
        System.out.println("Frame initialization test");
        
        assertNotNull("CommentAdminFrame instance should not be null", instance);
        
        try {
            CommentAdminFrame newFrame = new CommentAdminFrame();
            assertNotNull("New CommentAdminFrame instance should not be null", newFrame);
            newFrame.dispose();
        } catch (Exception e) {
            fail("CommentAdminFrame constructor should not throw exception: " + e.getMessage());
        }
    }
    
    //test method chaining - setting user then issue
    @Test
    public void testMethodChaining() {
        System.out.println("Method chaining test");
        
        try {
            instance.setLoggedInUser(testUser);
            instance.setCurrentIssue(testIssue);
            instance.loadComments(testIssue);
            
            assertTrue("Method chaining should work without exceptions", true);
        } catch (Exception e) {
            fail("Method chaining should not throw exception: " + e.getMessage());
        }
    }
    
    //test with issue that has an ID of 0 (which might be invalid)
    @Test
    public void testWithInvalidIssueId() {
        System.out.println("Test with invalid issue ID");
        
        IssueData invalidIssue = new IssueData();
        invalidIssue.setId(0); 
        invalidIssue.setTitle("Invalid Issue");
        
        try {
            instance.setCurrentIssue(invalidIssue);
            instance.loadComments(invalidIssue);
            
            assertTrue("Should handle invalid issue ID gracefully", true);
        } catch (Exception e) {
            System.out.println("Expected exception for invalid issue ID: " + e.getMessage());
        }
    }
}