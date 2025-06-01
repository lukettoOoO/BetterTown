package com.mycompany.bettertown;

import java.time.LocalDateTime;
import javax.swing.ImageIcon;
import org.junit.Test;
import static org.junit.Assert.*;

public class IssueDataTest {

    @Test
    public void testGettersAndSetters() {
        IssueData issue = new IssueData();

        int id = 123;
        String title = "Pothole";
        String description = "There's a large pothole on the main road.";
        ImageIcon image = new ImageIcon(); //mock image
        int priority = 2;
        String city = "Bettertown";
        String address = "Main St 12";
        LocalDateTime date = LocalDateTime.now();
        String username = "user123";
        String status = "Open";
        double longitude = 21.2345;
        double latitude = 45.6789;

        // Set values
        issue.setId(id);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setPhoto(image);
        issue.setPriority(priority);
        issue.setCity(city);
        issue.setAddress(address);
        issue.setDate(date);
        issue.setUsername(username);
        issue.setStatus(status);
        issue.setLongitude(longitude);
        issue.setLatitude(latitude);

        // Assert values
        assertEquals(id, issue.getId());
        assertEquals(title, issue.getTitle());
        assertEquals(description, issue.getDescription());
        assertEquals(image, issue.getPhoto());
        assertEquals(priority, issue.getPriority());
        assertEquals(city, issue.getCity());
        assertEquals(address, issue.getAddress());
        assertEquals(date, issue.getDate());
        assertEquals(username, issue.getUsername());
        assertEquals(status, issue.getStatus());
        assertEquals(longitude, issue.getLongitude(), 0.0001);
        assertEquals(latitude, issue.getLatitude(), 0.0001);
    }
}
