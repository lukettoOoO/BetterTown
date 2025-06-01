package com.mycompany.bettertown.map;

import com.mycompany.bettertown.IssueData;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.swing.JButton;

public class MyWaypointTest {

    //mock class for EventWaypoint
    private static class MockEventWaypoint implements EventWaypoint {
        public boolean wasSelectedCalled = false;

        @Override
        public void selected(MyWaypoint waypoint) {
            wasSelectedCalled = true;
        }
    }

    @Test
    public void testMyWaypointConstructorAndGetters() {
        IssueData data = new IssueData();
        data.setTitle("Pothole");

        GeoPosition coord = new GeoPosition(45.75, 21.23);
        MockEventWaypoint mockEvent = new MockEventWaypoint();

        MyWaypoint waypoint = new MyWaypoint(data, mockEvent, coord);

        assertEquals("Pothole", waypoint.getData().getTitle());
        assertEquals(coord, waypoint.getPosition());
        assertNotNull(waypoint.getButton());
    }

    @Test
    public void testButtonActionListenerTriggersEvent() {
        IssueData data = new IssueData();
        GeoPosition coord = new GeoPosition(45.75, 21.23);
        MockEventWaypoint mockEvent = new MockEventWaypoint();

        MyWaypoint waypoint = new MyWaypoint(data, mockEvent, coord);
        JButton button = waypoint.getButton();

        // Simulăm apăsarea butonului
        button.doClick();

        assertTrue(mockEvent.wasSelectedCalled);
    }

    @Test
    public void testSetAndGetButton() {
        GeoPosition coord = new GeoPosition(0, 0);
        MyWaypoint waypoint = new MyWaypoint(new IssueData(), new MockEventWaypoint(), coord);
        JButton newButton = new JButton("Test");
        waypoint.setButton(newButton);
        assertEquals(newButton, waypoint.getButton());
    }

    @Test
    public void testSetAndGetData() {
        MyWaypoint waypoint = new MyWaypoint(new IssueData(), new MockEventWaypoint(), new GeoPosition(0, 0));
        IssueData newData = new IssueData();
        newData.setTitle("Graffiti");
        waypoint.setData(newData);
        assertEquals("Graffiti", waypoint.getData().getTitle());
    }
}
