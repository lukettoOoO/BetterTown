/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bettertown;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
/**
 *
 * @author biancagrovu
 */
public class ImageConverter {
    public static byte[] imageIconToByteArray(ImageIcon icon, String format) throws IOException {
        // Create a BufferedImage from the ImageIcon
        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB); // Or TYPE_INT_RGB if no transparency needed

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(icon.getImage(), 0, 0, null);
        g2d.dispose();

        // Write the BufferedImage to a ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, baos); // "png" or "jpeg"
        return baos.toByteArray();
    }
    public static ImageIcon byteArrayToImageIcon(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(bais); // Read the image from the byte stream
        if (bufferedImage != null) {
            return new ImageIcon(bufferedImage); // Create ImageIcon from BufferedImage
        }
        return null;
    }
}
