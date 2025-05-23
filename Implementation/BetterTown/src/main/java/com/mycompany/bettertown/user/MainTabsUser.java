/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.bettertown.user;
import com.mycompany.bettertown.login.DatabaseLogic;

import com.github.kevinsawicki.http.HttpRequest;
import com.mycompany.bettertown.IssueData;
import com.mycompany.bettertown.login.LoginFrame;
import com.mycompany.bettertown.login.LogoutConfirmationFrame;
import com.mycompany.bettertown.login.LogoutListener;
import com.mycompany.bettertown.login.ProfileData;
import com.mycompany.bettertown.map.EventWaypoint;
import com.mycompany.bettertown.map.MyWaypoint;
import com.mycompany.bettertown.map.WaypointRender;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmapviewer.viewer.WaypointPainter;
import java.util.List;


/**
 *
 * @author luca
 */
public class MainTabsUser extends javax.swing.JFrame {

    /**
     * Creates new form MainTabs
     */
    
    private JXMapViewer mapViewer;
    private final ImageIcon logoIcon;
    private double currentLatitude;
    private double currentLongitude;
    private final Set<MyWaypoint> waypoints = new HashSet<>();
    private ArrayList<IssueData> issueDataList = new ArrayList<IssueData>(); //this list and its contents has to be exported and imported from and to database
    private EventWaypoint event;
    private ProfileData currentUserData;
    
    public MainTabsUser() {
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
        
        //get all issue data from database
        //Bia modified here, used a method from DatabaseLogic that returns a list of all the issues 
        //that are currently saved in the table calles issue
        List<IssueData> savedIssues = DatabaseLogic.getAllIssues();

        for (IssueData issue : savedIssues) {
            GeoPosition pos = new GeoPosition(issue.getLatitude(), issue.getLongitude());
            issueDataList.add(issue); // opțional, dacă vrei să le ai și în listă
            addWaypoint(new MyWaypoint(issue, event, pos));
        }
        initWaypoint();
        //mouse listener for getting coordinates/ location from API and adding a waypoint:
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
                        //set the correct latitude and longitude
                        issueData.setLatitude(currentLatitude);
                        issueData.setLongitude(currentLongitude);
    
                        DatabaseLogic.saveWaypoint(issueData);
    
                        issueDataList.add(issueData);
                        printCurrentIssues();
                        addWaypoint(new MyWaypoint(issueData, event, new GeoPosition(currentLatitude, currentLongitude)));
                        initWaypoint();
                    }
                });
                addIssueForm.setCity(getCity(geo));
                addIssueForm.setAddress(getLocation(geo));
                addIssueForm.setUserName(currentUserData.getName());
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
    
    private void addWaypoint(MyWaypoint waypoint)
    {
        for(MyWaypoint d : waypoints)
        {
            mapViewer.remove(d.getButton());
        }
        waypoints.add(waypoint);
        initWaypoint();
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
  
    public void setCurrentUserData(ProfileData profileData)
    {
        this.currentUserData = profileData;
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
        
        ImageIcon feedbackIcon = new ImageIcon("feedback.png");
        feedbackButton.setIcon(feedbackIcon);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel14 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        mapButton = new javax.swing.JButton();
        feedButton = new javax.swing.JButton();
        alertsButton = new javax.swing.JButton();
        feedbackButton = new javax.swing.JButton();
        logoLabel = new javax.swing.JLabel();
        logOutButton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        mapPanel = new javax.swing.JPanel();
        comboMapType = new javax.swing.JComboBox<>();
        mapPanelSecondary = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        feedPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        reportButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
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
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        viewButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        alertsLabel = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        feedbackLabel = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jLabel10 = new javax.swing.JLabel();
        starButton1 = new javax.swing.JButton();
        starButton2 = new javax.swing.JButton();
        starButton3 = new javax.swing.JButton();
        starButton4 = new javax.swing.JButton();
        starButton5 = new javax.swing.JButton();

        jLabel14.setText("jLabel14");

        jLabel19.setText("jLabel19");

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

        feedbackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedbackButtonActionPerformed(evt);
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
            .addComponent(feedbackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(feedbackButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logOutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        mapPanel.setBackground(new java.awt.Color(255, 255, 255));

        comboMapType.setEditable(true);
        comboMapType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Open Street", "Virtual Earth", "Hybrid", "Satelite" }));
        comboMapType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMapTypeActionPerformed(evt);
            }
        });

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

        jLabel4.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel4.setText("Map view:");

        jLabel5.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel5.setText("Report an issue on map:");

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mapPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 266, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(10, 10, 10)
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

        reportButton.setText("Report an issue...");
        reportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportButtonActionPerformed(evt);
            }
        });

        jSeparator1.setBackground(new java.awt.Color(204, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(51, 204, 255));

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

        jButton2.setBackground(new java.awt.Color(204, 255, 204));
        jButton2.setText("⬆");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 204, 204));
        jButton3.setText("⬇");

        jLabel3.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 13)); // NOI18N
        jLabel3.setText("0");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(reportButton)
                .addGap(23, 23, 23))
            .addComponent(jSeparator1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(feedPanelLayout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3))
                            .addComponent(viewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(145, 145, 145)
                        .addComponent(viewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton3)
                            .addComponent(jLabel3))))
                .addGap(35, 35, 35))
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
                        .addGap(0, 473, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        alertsLabelLayout.setVerticalGroup(
            alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5)
                    .addComponent(jButton4))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Alerts", alertsLabel);

        feedbackLabel.setBackground(new java.awt.Color(255, 255, 255));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel6.setText("Resolved issues:");

        jLabel7.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel7.setText("Subject:");

        jLabel8.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel8.setText("Description:");

        jLabel9.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel9.setText("Resolved by:");

        jButton6.setText("Send Feedback");

        jScrollPane3.setViewportView(jEditorPane1);

        jLabel10.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel10.setText("Rating:");

        starButton1.setText("☆");
        starButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starButton1ActionPerformed(evt);
            }
        });

        starButton2.setText("☆");
        starButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starButton2ActionPerformed(evt);
            }
        });

        starButton3.setText("☆");
        starButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starButton3ActionPerformed(evt);
            }
        });

        starButton4.setText("☆");
        starButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starButton4ActionPerformed(evt);
            }
        });

        starButton5.setText("☆");
        starButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout feedbackLabelLayout = new javax.swing.GroupLayout(feedbackLabel);
        feedbackLabel.setLayout(feedbackLabelLayout);
        feedbackLabelLayout.setHorizontalGroup(
            feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackLabelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton6)
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(feedbackLabelLayout.createSequentialGroup()
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9))
                            .addComponent(jTextField2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                            .addGroup(feedbackLabelLayout.createSequentialGroup()
                                .addComponent(starButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(starButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(starButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(starButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(starButton5)))))
                .addContainerGap(94, Short.MAX_VALUE))
        );
        feedbackLabelLayout.setVerticalGroup(
            feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackLabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9))
                .addGap(12, 12, 12)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(starButton1)
                    .addComponent(starButton2)
                    .addComponent(starButton3)
                    .addComponent(starButton4)
                    .addComponent(starButton5))
                .addGap(13, 13, 13)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap(129, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Feedback", feedbackLabel);

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

    private void feedbackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedbackButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(3);
        
        starButton1.setBackground(Color.white);
        starButton2.setBackground(Color.white);
        starButton3.setBackground(Color.white);
        starButton4.setBackground(Color.white);
        starButton5.setBackground(Color.white);
    }//GEN-LAST:event_feedbackButtonActionPerformed

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

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_reportButtonActionPerformed

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_viewButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        CommentUserFrame commentObj = new CommentUserFrame();
        commentObj.show();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void starButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starButton1ActionPerformed
        // TODO add your handling code here:
        starButton1.setText("★");
        starButton1.setBackground(Color.yellow);
        
        starButton5.setText("☆");
        starButton5.setBackground(Color.white);
        starButton4.setText("☆");
        starButton4.setBackground(Color.white);
        starButton3.setText("☆");
        starButton3.setBackground(Color.white);
        starButton2.setText("☆");
        starButton2.setBackground(Color.white);
    }//GEN-LAST:event_starButton1ActionPerformed

    private void starButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starButton2ActionPerformed
        // TODO add your handling code here:
        starButton1.setText("★");
        starButton1.setBackground(Color.yellow);
        starButton2.setText("★");
        starButton2.setBackground(Color.yellow);
        
        starButton5.setText("☆");
        starButton5.setBackground(Color.white);
        starButton4.setText("☆");
        starButton4.setBackground(Color.white);
        starButton3.setText("☆");
        starButton3.setBackground(Color.white);
    }//GEN-LAST:event_starButton2ActionPerformed

    private void starButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starButton3ActionPerformed
        // TODO add your handling code here:
        starButton1.setText("★");
        starButton1.setBackground(Color.yellow);
        starButton2.setText("★");
        starButton2.setBackground(Color.yellow);
        starButton3.setText("★");
        starButton3.setBackground(Color.yellow);
        
        starButton5.setText("☆");
        starButton5.setBackground(Color.white);
        starButton4.setText("☆");
        starButton4.setBackground(Color.white);
    }//GEN-LAST:event_starButton3ActionPerformed

    private void starButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starButton4ActionPerformed
        // TODO add your handling code here:
        starButton1.setText("★");
        starButton1.setBackground(Color.yellow);
        starButton2.setText("★");
        starButton2.setBackground(Color.yellow);
        starButton3.setText("★");
        starButton3.setBackground(Color.yellow);
        starButton4.setText("★");
        starButton4.setBackground(Color.yellow);
        
        starButton5.setText("☆");
        starButton5.setBackground(Color.white);
    }//GEN-LAST:event_starButton4ActionPerformed

    private void starButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starButton5ActionPerformed
        // TODO add your handling code here:
        starButton1.setText("★");
        starButton1.setBackground(Color.yellow);
        starButton2.setText("★");
        starButton2.setBackground(Color.yellow);
        starButton3.setText("★");
        starButton3.setBackground(Color.yellow);
        starButton4.setText("★");
        starButton4.setBackground(Color.yellow);
        starButton5.setText("★");
        starButton5.setBackground(Color.yellow);
    }//GEN-LAST:event_starButton5ActionPerformed

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
            java.util.logging.Logger.getLogger(MainTabsUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainTabsUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainTabsUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainTabsUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainTabsUser().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton alertsButton;
    private javax.swing.JPanel alertsLabel;
    private javax.swing.JComboBox<String> comboMapType;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JButton feedButton;
    private javax.swing.JPanel feedPanel;
    private javax.swing.JButton feedbackButton;
    private javax.swing.JPanel feedbackLabel;
    private javax.swing.JPanel issueViewPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JButton logOutButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JButton mapButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JPanel mapPanelSecondary;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JButton reportButton;
    private javax.swing.JButton starButton1;
    private javax.swing.JButton starButton2;
    private javax.swing.JButton starButton3;
    private javax.swing.JButton starButton4;
    private javax.swing.JButton starButton5;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables

}
