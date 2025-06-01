package com.mycompany.bettertown;

import javax.swing.ImageIcon;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class ImageConverterTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    //helper method to create a simple dummy ImageIcon
    private ImageIcon createDummyImageIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.dispose();
        return new ImageIcon(img);
    }

    //tests for imageIconToByteArray method
    
    //test of imageIconToByteArray method with a valid ImageIcon and PNG format
    @Test
    public void testImageIconToByteArray_PngFormat() throws IOException {
        System.out.println("imageIconToByteArray - PNG format");

        //arrange
        ImageIcon icon = createDummyImageIcon(50, 50); // Create a 50x50 dummy image
        String format = "png";

        //act
        byte[] result = ImageConverter.imageIconToByteArray(icon, format);

        //assert
        assertNotNull("The byte array should not be null", result);
        assertTrue("The byte array should not be empty", result.length > 0);

        ImageIcon convertedBackIcon = ImageConverter.byteArrayToImageIcon(result);
        assertNotNull("The converted back icon should not be null", convertedBackIcon);
        assertEquals("Converted icon width should match original", icon.getIconWidth(), convertedBackIcon.getIconWidth());
        assertEquals("Converted icon height should match original", icon.getIconHeight(), convertedBackIcon.getIconHeight());
    }

    //test of imageIconToByteArray method with null ImageIcon input, expects a NullPointerException, as the method doesn't handle null gracefully
    @Test(expected = NullPointerException.class)
    public void testImageIconToByteArray_NullIcon() throws IOException {
        System.out.println("imageIconToByteArray - Null icon");
        ImageConverter.imageIconToByteArray(null, "png");
    }
    
    //tests for byteArrayToImageIcon method

    //test of byteArrayToImageIcon method with a valid byte array
    @Test
    public void testByteArrayToImageIcon_ValidBytes() throws IOException {
        System.out.println("byteArrayToImageIcon - Valid bytes");

        ImageIcon originalIcon = createDummyImageIcon(60, 40);
        byte[] imageBytes = ImageConverter.imageIconToByteArray(originalIcon, "png");

        ImageIcon resultIcon = ImageConverter.byteArrayToImageIcon(imageBytes);

        assertNotNull("The result ImageIcon should not be null", resultIcon);
        assertEquals("The width of the converted image should match original",
                     originalIcon.getIconWidth(), resultIcon.getIconWidth());
        assertEquals("The height of the converted image should match original",
                     originalIcon.getIconHeight(), resultIcon.getIconHeight());
    }

    //test of byteArrayToImageIcon method with null byte array
    @Test
    public void testByteArrayToImageIcon_NullBytes() throws IOException {
        System.out.println("byteArrayToImageIcon - Null bytes");

        ImageIcon result = ImageConverter.byteArrayToImageIcon(null);

        assertNull("The result ImageIcon should be null for null input bytes", result);
    }

    //test of byteArrayToImageIcon method with an empty byte array
    @Test
    public void testByteArrayToImageIcon_EmptyBytes() throws IOException {
        System.out.println("byteArrayToImageIcon - Empty bytes");

        ImageIcon result = ImageConverter.byteArrayToImageIcon(new byte[0]);

        assertNull("The result ImageIcon should be null for empty input bytes", result);
    }

    //test of byteArrayToImageIcon method with invalid image bytes
    @Test
    public void testByteArrayToImageIcon_InvalidImageBytes() throws IOException {
        System.out.println("byteArrayToImageIcon - Invalid image bytes");

        byte[] invalidBytes = "This is not an image".getBytes();

        ImageIcon result = ImageConverter.byteArrayToImageIcon(invalidBytes);

        assertNull("The result ImageIcon should be null for invalid image bytes", result);
    }
}