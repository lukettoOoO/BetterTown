package com.mycompany.bettertown.user;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.image.BufferedImage; 
import java.awt.Graphics;

public class PhotoViewTest {

    private PhotoView instance;

    public PhotoViewTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        instance = new PhotoView();
        instance.setVisible(true);
    }

    @After
    public void tearDown() throws Exception {
        if (instance != null) {
            instance.dispose();
        }
    }

    @Test
    public void testSetImage() {
        System.out.println("setImage");

        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics g = dummyImage.createGraphics();
        g.dispose();
        ImageIcon expectedIcon = new ImageIcon(dummyImage);

        instance.setImage(expectedIcon);

        try {
            java.lang.reflect.Field field = PhotoView.class.getDeclaredField("PhotoLabel");
            field.setAccessible(true); // corect: "PhotoLabel" cu majusculă
            JLabel actualPhotoLabel = (JLabel) field.get(instance);
            assertNotNull("PhotoLabel should not be null after initialization", actualPhotoLabel);
            assertEquals("The image icon on PhotoLabel should match the one set by setImage",
                         expectedIcon, actualPhotoLabel.getIcon());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access PhotoLabel: " + e.getMessage());
        }
    }

    @Test
    public void testSetImageNull() {
        System.out.println("setImage with null");

        ImageIcon nullIcon = null;
        instance.setImage(nullIcon);

        try {
            java.lang.reflect.Field field = PhotoView.class.getDeclaredField("PhotoLabel");
            field.setAccessible(true);
            JLabel actualPhotoLabel = (JLabel) field.get(instance);
            assertNotNull("PhotoLabel should not be null after initialization", actualPhotoLabel);
            assertNull("The image icon on PhotoLabel should be null when setImage is called with null",
                       actualPhotoLabel.getIcon());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access PhotoLabel: " + e.getMessage());
        }
    }

    @Test
    public void testMain() {
        System.out.println("main");
        // Optional: testarea metodei main, dar e gol aici
    }
}
