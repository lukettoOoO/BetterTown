/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.bettertown.admin;

import com.github.kevinsawicki.http.HttpRequest;
import com.mycompany.bettertown.IssueData;
import com.mycompany.bettertown.login.LogoutConfirmationFrame;
import com.mycompany.bettertown.login.LogoutListener;
import com.mycompany.bettertown.login.ProfileData;
import com.mycompany.bettertown.map.EventWaypoint;
import com.mycompany.bettertown.map.MyWaypoint;
import com.mycompany.bettertown.map.WaypointRender;
import com.mycompany.bettertown.user.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

/**
 *
 * @author luca
 */
public class MainTabsAdmin extends javax.swing.JFrame {

    /**
     * Creates new form MainTabs
     */
    
    private JXMapViewer mapViewer;
    private final ImageIcon logoIcon;
    private double currentLatitude;
    private double currentLongitude;
    private final Set<MyWaypoint> waypoints = new HashSet<>();
    private ArrayList<IssueData> issueDataList = new ArrayList<IssueData>();
    private EventWaypoint event;
    private ProfileData currentAdminData;
    
    public MainTabsAdmin() {
        initComponents();
        
        this.logoIcon = new ImageIcon("logo.png");
        logoLabel.setIcon(logoIcon);
        setLocationRelativeTo(null);
        
        initMap();
        initButtons();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //to be implemented: get profile data from login form
        
    }
    
    private void initMap()
    {
        //map init:
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(8);
        GeoPosition timisoara = new GeoPosition(45.75, 21.23);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(timisoara);
        mapPanelSecondary.setLayout(new BorderLayout());
        mapPanelSecondary.add(mapViewer, BorderLayout.CENTER);
        mapPanelSecondary.setLayout(new java.awt.BorderLayout());
        mapPanelSecondary.add(mapViewer, java.awt.BorderLayout.CENTER);
        
        //mouse move:
        MouseInputListener mouseMove = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mouseMove);
        mapViewer.addMouseMotionListener(mouseMove);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        
        //mouse listener for getting coordinates and adding a waypoint:
        mapViewer.addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1){
                java.awt.Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                currentLatitude = geo.getLatitude();
                currentLongitude = geo.getLongitude();
                System.out.println("X:" + currentLatitude + ",Y:" + currentLongitude);
                
                showLocation(geo); //API for location
                AddIssue addIssueForm = new AddIssue(new AddIssueListener() {
                    @Override
                    public void onIssueAdded(IssueData issueData) {
                        // Set the correct latitude and longitude
                        issueData.setLatitude(currentLatitude);
                        issueData.setLongitude(currentLongitude);

                        // Add to the issue list BIA
                        issueDataList.add(issueData);
                        printCurrentIssues();

                        // Add a waypoint to the map
                        addWaypoint(new MyWaypoint(issueData, event, new GeoPosition(currentLatitude, currentLongitude)));
                        initWaypoint();
                    }
               });
                addIssueForm.setCity(getCity(geo));
                addIssueForm.setAddress(getLocation(geo));
                addIssueForm.setUserName(currentAdminData.getName());
                addIssueForm.show();
            } 
        }
        }); 
        
        event = getEvent();
    }
    
    private void initWaypoint()
    {
        WaypointPainter<MyWaypoint> wp = new WaypointRender();
        wp.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(wp);
        for(MyWaypoint d : waypoints)
        {
            mapViewer.add(d.getButton());
        }
    }
    
    public void clearWaypoint()
    {
        for(MyWaypoint d : waypoints)
        {
            mapViewer.remove(d.getButton());
        }
        waypoints.clear();
        initWaypoint();
    }
    
     private EventWaypoint getEvent()
    {
        return new EventWaypoint()
        {
            public void selected(MyWaypoint waypoint)
            {
                tabbedPane.setSelectedIndex(1);
            }
        };
    }
    //admins shold NOT be able to add waypoints, only EDIT them
     //this method is for testing puroposes only
    private void addWaypoint(MyWaypoint waypoint)
    {
        for(MyWaypoint d : waypoints)
        {
            mapViewer.remove(d.getButton());
        }
        waypoints.add(waypoint);
        initWaypoint();
    }
    
    private double getCurrentLatitude()
    {
        return currentLatitude;
    }
    
    private double getCurrentLongitude()
    {
        return currentLongitude;
    }
    
    private void showLocation(GeoPosition geo)
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    System.out.println(getLocation(geo));
                    System.out.println(getCity(geo));
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public String getLocation(GeoPosition pos) throws JSONException
    {
        String body = HttpRequest.get("https://nominatim.openstreetmap.org/reverse?lat=" + pos.getLatitude() + "&lon=" + pos.getLongitude() + "&format=json").body();
        JSONObject json = new JSONObject(body);
        return json.getString("display_name");
    }
    
  public String getCity(GeoPosition pos) throws JSONException 
  {
    String body = HttpRequest.get("https://nominatim.openstreetmap.org/reverse?lat=" + pos.getLatitude() + "&lon=" + pos.getLongitude() + "&format=json").body();
    JSONObject json = new JSONObject(body);
    JSONObject address = json.getJSONObject("address");
    String city = address.optString("city"); // Use optString to handle cases where "city" might be missing
    if (city == null || city.isEmpty()) {
        city = address.optString("village"); // Try to get village if city is not found
    }
    return city;
}
  
    public void setCurrentAdminData(ProfileData profileData)
    {
        this.currentAdminData = profileData;
    }
    
    private void printCurrentIssues()
    {
        if (issueDataList.isEmpty()) 
        {
        System.out.println("No issues to display.");
        return;
        }
        System.out.println("CURRENT ISSUES:");
        for (IssueData issue : issueDataList) 
        {
            System.out.println("Title: " + issue.getTitle());
            System.out.println("Description: " + issue.getDescription());
            System.out.println("City: " + issue.getCity());
            System.out.println("Address: " + issue.getAddress());
            System.out.println("User Name: " + issue.getUsername());
            System.out.println("Status: " + issue.getStatus());
            System.out.println("Priority: " + issue.getPriority());
            System.out.println("Photo Path: " + issue.getPhoto());
            System.out.println("Date: " + issue.getDate());
            System.out.println("Latitude: " + issue.getLatitude());
            System.out.println("Longitude: " + issue.getLongitude());
            System.out.println("-----------------------------------");
        }
    }
    
    private void initButtons()
    {
        ImageIcon mapIcon = new ImageIcon("map.png");
        mapButton.setIcon(mapIcon);
        
        ImageIcon feedIcon = new ImageIcon("feed.png");
        feedButton.setIcon(feedIcon);
        
        ImageIcon alertsIcon = new ImageIcon("alerts.png");
        alertsButton.setIcon(alertsIcon);
        
        ImageIcon managerIcon = new ImageIcon("manager.png");
        managerButton.setIcon(managerIcon);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        mapButton = new javax.swing.JButton();
        feedButton = new javax.swing.JButton();
        alertsButton = new javax.swing.JButton();
        managerButton = new javax.swing.JButton();
        logoLabel = new javax.swing.JLabel();
        logOutButton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        mapPanel = new javax.swing.JPanel();
        comboMapType = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        mapPanelSecondary = new javax.swing.JPanel();
        feedPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        viewButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        issueViewPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        photoLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel18 = new javax.swing.JLabel();
        titleLabel1 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        deleteAllButton = new javax.swing.JButton();
        alertsLabel = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        feedbackLabel = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList<>();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(64, 64, 64));

        mapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapButtonActionPerformed(evt);
            }
        });

        feedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedButtonActionPerformed(evt);
            }
        });

        alertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alertsButtonActionPerformed(evt);
            }
        });

        managerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managerButtonActionPerformed(evt);
            }
        });

        logOutButton.setText("Log out");
        logOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logOutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(feedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(alertsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(managerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(logOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(mapButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(feedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alertsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(managerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logOutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        mapPanel.setBackground(new java.awt.Color(255, 255, 255));

        comboMapType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Open Street", "Virtual Earth", "Hybrid", "Satelite" }));
        comboMapType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMapTypeActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel4.setText("Map view:");

        jLabel5.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel5.setText("Update reported issues on map:");

        mapPanelSecondary.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mapPanelSecondary.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mapPanelSecondaryMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mapPanelSecondaryLayout = new javax.swing.GroupLayout(mapPanelSecondary);
        mapPanelSecondary.setLayout(mapPanelSecondaryLayout);
        mapPanelSecondaryLayout.setHorizontalGroup(
            mapPanelSecondaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        mapPanelSecondaryLayout.setVerticalGroup(
            mapPanelSecondaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 448, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mapPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 181, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        tabbedPane.addTab("Map", mapPanel);

        feedPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel1.setText("Search an issue:");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel2.setText("by:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Title", "Priority", "City", "Address", "Date", "Name", "Status" }));

        jSeparator1.setBackground(new java.awt.Color(204, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(51, 204, 255));

        jList1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Issue List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 18), new java.awt.Color(51, 204, 255))); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "6", "7", "3", "2", "3", "4", "5", "32", "2", "1", "1", "34", " " };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jList1);

        viewButton.setText("View on map...");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        jButton1.setText("View comments");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton7.setText("Delete");

        jButton8.setText("Update");

        issueViewPanel.setBackground(new java.awt.Color(255, 255, 255));
        issueViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Issue View", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 18), new java.awt.Color(0, 204, 255))); // NOI18N

        jLabel11.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel11.setText("Title:");

        titleLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        titleLabel.setText("Issue Title");

        jLabel13.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel13.setText("Description:");
        jLabel13.setToolTipText("");

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setBackground(new java.awt.Color(255, 255, 255));
        descriptionTextArea.setColumns(20);
        descriptionTextArea.setFont(new java.awt.Font("Apple SD Gothic Neo", 0, 12)); // NOI18N
        descriptionTextArea.setRows(5);
        jScrollPane4.setViewportView(descriptionTextArea);

        jLabel15.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel15.setText("Photo:");
        jLabel15.setToolTipText("");

        photoLabel.setBackground(new java.awt.Color(204, 204, 204));
        photoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                photoLabelMouseClicked(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel12.setText("City:");

        jLabel16.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        jLabel16.setText("City Name");

        jLabel17.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel17.setText("Address:");

        jTextPane1.setEditable(false);
        jTextPane1.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jScrollPane5.setViewportView(jTextPane1);

        jLabel18.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel18.setText("Priority:");

        titleLabel1.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        titleLabel1.setText("0");

        jLabel20.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel20.setText("Reported by:");

        jLabel21.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel21.setText("Date:");

        jLabel22.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        jLabel22.setText("User Name");

        jLabel23.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        jLabel23.setText("Date");

        jLabel24.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel24.setText("Status:");

        jLabel25.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        jLabel25.setText("Status");

        javax.swing.GroupLayout issueViewPanelLayout = new javax.swing.GroupLayout(issueViewPanel);
        issueViewPanel.setLayout(issueViewPanelLayout);
        issueViewPanelLayout.setHorizontalGroup(
            issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issueViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel16)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(titleLabel))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(titleLabel1))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel25)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        issueViewPanelLayout.setVerticalGroup(
            issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issueViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(titleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel16))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(34, 34, 34))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(photoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(titleLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25))
                .addContainerGap())
        );

        deleteAllButton.setText("Delete All <Test>");
        deleteAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout feedPanelLayout = new javax.swing.GroupLayout(feedPanel);
        feedPanel.setLayout(feedPanelLayout);
        feedPanelLayout.setHorizontalGroup(
            feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 250, Short.MAX_VALUE))
            .addComponent(jSeparator1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(viewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1))
                            .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteAllButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        feedPanelLayout.setVerticalGroup(
            feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(viewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addGap(47, 47, 47))))
        );

        tabbedPane.addTab("Feed", feedPanel);

        alertsLabel.setBackground(new java.awt.Color(255, 255, 255));

        jButton4.setText("Select All");

        jButton5.setText("Delete");
        jButton5.setToolTipText("");

        jList2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Alert List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 24), new java.awt.Color(51, 204, 255))); // NOI18N
        jList2.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jList2.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "6", "7", "3", "2", "3", "4", "5", "32", "2", "1", "1", "34", "ef", "ewf", "a", "edf", "awr", "g", "erg", "er", "hg", "ert", "h", "eth", "etherg", "a", "reg", "reg", "arg", " " };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList2);

        javax.swing.GroupLayout alertsLabelLayout = new javax.swing.GroupLayout(alertsLabel);
        alertsLabel.setLayout(alertsLabelLayout);
        alertsLabelLayout.setHorizontalGroup(
            alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alertsLabelLayout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)
                        .addGap(0, 440, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        alertsLabelLayout.setVerticalGroup(
            alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5)
                    .addComponent(jButton4))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Alerts", alertsLabel);

        feedbackLabel.setBackground(new java.awt.Color(255, 255, 255));

        jSeparator2.setForeground(new java.awt.Color(0, 204, 255));

        jLabel3.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 18)); // NOI18N
        jLabel3.setText("Statistics:");

        jButton2.setText("View Statistics");

        jLabel6.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 18)); // NOI18N
        jLabel6.setText("Manage Accounts:");

        jList3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Alert List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 24), new java.awt.Color(51, 204, 255))); // NOI18N
        jList3.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 14)); // NOI18N
        jList3.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "6", "7", "3", "2", "3", "4", "5", "32", "2", "1", "1", "34", "ef", "ewf", "a", "edf", "awr", "g", "erg", "er", "hg", "ert", "h", "eth", "etherg", "a", "reg", "reg", "arg", " " };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList3);

        jButton3.setForeground(new java.awt.Color(255, 0, 0));
        jButton3.setText("Delete");

        jButton6.setForeground(new java.awt.Color(255, 0, 0));
        jButton6.setText("Block");

        jTextField2.setText("Search...");

        jButton9.setText("Edit");

        jButton10.setText("Password Reset");

        jLabel7.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 18)); // NOI18N
        jLabel7.setText("Feedback:");

        jButton11.setText("View Feedback");

        javax.swing.GroupLayout feedbackLabelLayout = new javax.swing.GroupLayout(feedbackLabel);
        feedbackLabel.setLayout(feedbackLabelLayout);
        feedbackLabelLayout.setHorizontalGroup(
            feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addGroup(feedbackLabelLayout.createSequentialGroup()
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addGap(73, 73, 73)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton11))
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel6))
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        feedbackLabelLayout.setVerticalGroup(
            feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackLabelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jButton2)
                    .addComponent(jLabel7)
                    .addComponent(jButton11))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addGap(33, 33, 33)
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10)))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Manager", feedbackLabel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 528, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_mapButtonActionPerformed

    private void feedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(1);
    }//GEN-LAST:event_feedButtonActionPerformed

    private void alertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alertsButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(2);
    }//GEN-LAST:event_alertsButtonActionPerformed

    private void managerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managerButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(3);
        
    }//GEN-LAST:event_managerButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        CommentAdminFrame commentObj = new CommentAdminFrame();
        commentObj.show();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_viewButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void comboMapTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMapTypeActionPerformed
        // TODO add your handling code here:
        TileFactoryInfo info;
        int index = comboMapType.getSelectedIndex();
        switch (index) {
            case 0:
            info = new OSMTileFactoryInfo();
            break;
            case 1:
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
            break;
            case 2:
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
            break;
            default:
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);
            break;
        }
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
    }//GEN-LAST:event_comboMapTypeActionPerformed

    private void logOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logOutButtonActionPerformed
        LogoutConfirmationFrame logoutObj = new LogoutConfirmationFrame(new LogoutListener() {
        @Override
        public void onLogoutConfirmed() {
            dispose(); // Close the main frame
        }
        });
        logoutObj.setVisible(true);
    }//GEN-LAST:event_logOutButtonActionPerformed

    private void photoLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_photoLabelMouseClicked
        PhotoView photoViewObj = new PhotoView();
        photoViewObj.show();
    }//GEN-LAST:event_photoLabelMouseClicked

    private void mapPanelSecondaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelSecondaryMouseClicked

    }//GEN-LAST:event_mapPanelSecondaryMouseClicked

    private void deleteAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllButtonActionPerformed
        // TODO add your handling code here:
        clearWaypoint();
    }//GEN-LAST:event_deleteAllButtonActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainTabsAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainTabsAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainTabsAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainTabsAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainTabsAdmin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton alertsButton;
    private javax.swing.JPanel alertsLabel;
    private javax.swing.JComboBox<String> comboMapType;
    private javax.swing.JButton deleteAllButton;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JButton feedButton;
    private javax.swing.JPanel feedPanel;
    private javax.swing.JPanel feedbackLabel;
    private javax.swing.JPanel issueViewPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jList3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JButton logOutButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JButton managerButton;
    private javax.swing.JButton mapButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JPanel mapPanelSecondary;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables

}
