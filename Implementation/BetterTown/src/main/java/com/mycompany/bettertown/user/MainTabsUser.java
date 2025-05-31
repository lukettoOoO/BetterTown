/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.bettertown.user;

import com.github.kevinsawicki.http.HttpRequest;
import com.mycompany.bettertown.Feedback;
import com.mycompany.bettertown.IssueData;
import com.mycompany.bettertown.SolvedIssues;
import com.mycompany.bettertown.login.DatabaseLogic;
import com.mycompany.bettertown.login.LoginFrame;
import com.mycompany.bettertown.login.LogoutConfirmationFrame;
import com.mycompany.bettertown.login.LogoutListener;
import com.mycompany.bettertown.login.ProfileData;
import com.mycompany.bettertown.map.EventWaypoint;
import com.mycompany.bettertown.map.MyWaypoint;
import com.mycompany.bettertown.map.WaypointRender;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmapviewer.viewer.WaypointPainter;
import javax.swing.DefaultListModel;

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
    private DefaultListModel<String> issueViewListModel; //this is necessary for displaying the title of issues in the issue list JList in the feed
    private EventWaypoint event;
    private ProfileData currentUserData;
    private List<com.mycompany.bettertown.admin.Alerts> alerts=new ArrayList<com.mycompany.bettertown.admin.Alerts>();
    private DefaultListModel<String> alertsViewListModel;
    private List<SolvedIssues> solvedIssues=new ArrayList<SolvedIssues>();
    private DefaultComboBoxModel<String> solvedViewModel;
    private int rating=0;
    private ProfileData admin;
    private com.mycompany.bettertown.admin.Alerts alert=new com.mycompany.bettertown.admin.Alerts();
    
    public MainTabsUser() {
        initComponents();
        
        this.logoIcon = new ImageIcon("logo.png");
        logoLabel.setIcon(logoIcon);
        setLocationRelativeTo(null);
        
        issueViewListModel = new DefaultListModel<>();
        issueViewList.setModel(issueViewListModel);
        
        initMap();
        initButtons();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        if(statusLabel.getText().equals("Not resolved"))
        {
            statusLabel.setForeground(Color.RED);
        }
        else if(statusLabel.getText().equals("In progress"))
        {
            statusLabel.setForeground(Color.YELLOW);
        }
        else if(statusLabel.getText().equals("Resolved"))
        {
            statusLabel.setForeground(Color.GREEN);
        }
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
        event = getEvent();
        //mouse move:
        MouseInputListener mouseMove = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mouseMove);
        mapViewer.addMouseMotionListener(mouseMove);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        
        //get all issue data from DATABASE
        List<IssueData> savedIssues = DatabaseLogic.getAllIssues();

        for (IssueData issue : savedIssues) {
            GeoPosition pos = new GeoPosition(issue.getLatitude(), issue.getLongitude());
            issueDataList.add(issue);
            issueViewListModel.addElement(issue.getTitle());
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
                        
                        
                        
                        //add to the list and to feed
                        issueDataList.add(issueData); //add in the issue array list
                        issueViewListModel.addElement(issueData.getTitle()); //add the title of the issue in the feed issue lis
                        printCurrentIssues();
                        DatabaseLogic.saveWaypoint(issueData);
                        //add a waypoint on the map
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
    
    public void initAlerts(ProfileData currentUserData)
    {
        System.out.println(currentUserData.getName());
        alerts=DatabaseLogic.getAllAlertsFromDatabase(currentUserData);
        alertsViewListModel = new DefaultListModel<>();
        for(com.mycompany.bettertown.admin.Alerts a : alerts)
        {
            alertsViewListModel.addElement(a.getText());
        }
        AlertList.setModel(alertsViewListModel);
        System.out.println("Loaded alerts: "+ alerts.size());
    }
    
    public void initSolvedIssues()
    {
        solvedIssues=DatabaseLogic.getAllSolvedIssues();
        solvedViewModel=new DefaultComboBoxModel<>();
        for(SolvedIssues s : solvedIssues)
        {
            String title=DatabaseLogic.getIssuebyId(s);
            solvedViewModel.addElement(title);
        }
        ResolvedIssues.setModel(solvedViewModel);
        if(solvedIssues.size()>=1)
        {
            SolvedIssues issue=solvedIssues.get(0);
            ProfileData admin=DatabaseLogic.getUserById(issue.getUserId());
            AdminName.setText(admin.getName());
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
            public void selected(MyWaypoint waypoint) //selectarea unui waypoint pe harta
            {
                IssueData waypointIssueData = waypoint.getData();
                int selectedIndex = -1;
                for(int i = 0; i < issueViewListModel.getSize(); i++)
                {
                    if(waypointIssueData.getTitle().equals(issueViewListModel.getElementAt(i)))
                    {
                        selectedIndex = i;
                        break;
                    }
                }
                if(selectedIndex != -1)
                {
                    issueViewList.setSelectedIndex(selectedIndex);
                    issueViewList.ensureIndexIsVisible(selectedIndex);
                }
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
            System.out.println("Photo Path: " + issue.getImage());
            System.out.println("Date: " + issue.getDate());
            System.out.println("Latitude: " + issue.getLatitude());
            System.out.println("Longitude: " + issue.getLongitude());
            System.out.println("-----------------------------------");
        }
    }
    
    private GeoPosition searchLocation(String searchItem) {
    String body = null;
    try {
        body = HttpRequest.get("https://nominatim.openstreetmap.org/search.php?q=" + searchItem + "&format=json").body();
        JSONArray jsonArray = new JSONArray(body);

        if (jsonArray.length() > 0) {
            JSONObject firstResult = jsonArray.getJSONObject(0);
            double latitude = Double.parseDouble(firstResult.getString("lat"));
            double longitude = Double.parseDouble(firstResult.getString("lon"));
            return new GeoPosition(latitude, longitude);
        } else {
            System.out.println("Niciun rezultat găsit pentru: " + searchItem);
            return null;
        }

    } catch (org.json.JSONException e) {
        e.printStackTrace();
        System.err.println("Eroare la obținerea sau procesarea datelor de la Nominatim. Răspuns primit (dacă există): " + body);
        return null;
    }
}
    
    private IssueData getIssueAtSelectedJListIndex(int index)
    {
        int selectedIndex = index;
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    return issue;
                }
            }
        }
        return null;
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
        
        ImageIcon searchIcon = new ImageIcon("search.png");
        mapSearchButton.setIcon(searchIcon);
        
        feedSearchButton.setIcon(searchIcon);
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
        mapSearchButton = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        searchMapTextPane = new javax.swing.JTextPane();
        feedPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        feedSearchTextField = new javax.swing.JTextField();
        reportButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        issueViewPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        photoLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        cityLabel = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        priorityLabel = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        addressEditorPane = new javax.swing.JEditorPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        descriptionEditorPane = new javax.swing.JEditorPane();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        issueViewList = new javax.swing.JList<>();
        viewButton = new javax.swing.JButton();
        commentsButton = new javax.swing.JButton();
        feedSearchButton = new javax.swing.JButton();
        alertsLabel = new javax.swing.JPanel();
        DeleteAllAlertsButton = new javax.swing.JButton();
        DeleteAlertsButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        AlertList = new javax.swing.JList<>();
        feedbackLabel = new javax.swing.JPanel();
        ResolvedIssues = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        SubjectPane = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        ResolvedByLabel = new javax.swing.JLabel();
        SendFeedbackButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        DescriptionPane = new javax.swing.JEditorPane();
        jLabel10 = new javax.swing.JLabel();
        starButton1 = new javax.swing.JButton();
        starButton2 = new javax.swing.JButton();
        starButton3 = new javax.swing.JButton();
        starButton4 = new javax.swing.JButton();
        starButton5 = new javax.swing.JButton();
        AdminName = new javax.swing.JLabel();
        ErrorFeedback = new javax.swing.JLabel();

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
                .addGap(0, 24, Short.MAX_VALUE))
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
            .addGap(0, 446, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel4.setText("Map view:");

        jLabel5.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel5.setText("Report an issue on map:");

        mapSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSearchButtonActionPerformed(evt);
            }
        });

        searchMapTextPane.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 11)); // NOI18N
        searchMapTextPane.setToolTipText("");
        jScrollPane6.setViewportView(searchMapTextPane);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)))
                    .addComponent(mapSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Map", mapPanel);

        feedPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel1.setText("Search an issue:");

        feedSearchTextField.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                feedSearchTextFieldInputMethodTextChanged(evt);
            }
        });
        feedSearchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedSearchTextFieldActionPerformed(evt);
            }
        });

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

        jLabel13.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel13.setText("Description:");
        jLabel13.setToolTipText("");

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

        cityLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N

        jLabel17.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel17.setText("Address:");

        jLabel18.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel18.setText("Priority:");

        priorityLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N

        jLabel20.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel20.setText("Reported by:");

        jLabel21.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel21.setText("Date:");

        userLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N

        dateLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N

        jLabel24.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        jLabel24.setText("Status:");

        statusLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N

        addressEditorPane.setEditable(false);
        jScrollPane7.setViewportView(addressEditorPane);

        descriptionEditorPane.setEditable(false);
        jScrollPane5.setViewportView(descriptionEditorPane);

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
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cityLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(issueViewPanelLayout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane7))))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleLabel))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(priorityLabel))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateLabel))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userLabel))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel)))
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
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(92, 92, 92))
                    .addGroup(issueViewPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, issueViewPanelLayout.createSequentialGroup()
                                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(cityLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17)
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)))
                            .addComponent(photoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(priorityLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(dateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issueViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(statusLabel))
                .addContainerGap())
        );

        upButton.setBackground(new java.awt.Color(204, 255, 204));
        upButton.setText("⬆");
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setBackground(new java.awt.Color(255, 204, 204));
        downButton.setText("⬇");
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        issueViewList.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Issue List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 18), new java.awt.Color(51, 204, 255))); // NOI18N
        issueViewList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        issueViewList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                issueViewListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(issueViewList);

        viewButton.setText("View on map...");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        commentsButton.setText("View comments");
        commentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentsButtonActionPerformed(evt);
            }
        });

        feedSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedSearchButtonActionPerformed(evt);
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
                .addComponent(feedSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(feedSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(239, 239, 239)
                .addComponent(reportButton)
                .addGap(23, 23, 23))
            .addComponent(jSeparator1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(commentsButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(viewButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton)
                        .addGap(6, 6, 6)))
                .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        feedPanelLayout.setVerticalGroup(
            feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(feedSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(feedSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(reportButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(commentsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(upButton)
                            .addComponent(downButton))))
                .addGap(29, 29, 29))
        );

        tabbedPane.addTab("Feed", feedPanel);

        alertsLabel.setBackground(new java.awt.Color(255, 255, 255));

        DeleteAllAlertsButton.setText("Delete All");
        DeleteAllAlertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteAllAlertsButtonActionPerformed(evt);
            }
        });

        DeleteAlertsButton.setText("Delete");
        DeleteAlertsButton.setToolTipText("");
        DeleteAlertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteAlertsButtonActionPerformed(evt);
            }
        });

        AlertList.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Alert List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 24), new java.awt.Color(51, 204, 255))); // NOI18N
        AlertList.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        AlertList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "6", "7", "3", "2", "3", "4", "5", "32", "2", "1", "1", "34", "ef", "ewf", "a", "edf", "awr", "g", "erg", "er", "hg", "ert", "h", "eth", "etherg", "a", "reg", "reg", "arg", " " };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(AlertList);

        javax.swing.GroupLayout alertsLabelLayout = new javax.swing.GroupLayout(alertsLabel);
        alertsLabel.setLayout(alertsLabelLayout);
        alertsLabelLayout.setHorizontalGroup(
            alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alertsLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alertsLabelLayout.createSequentialGroup()
                        .addComponent(DeleteAllAlertsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DeleteAlertsButton)
                        .addGap(0, 499, Short.MAX_VALUE))
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
                    .addComponent(DeleteAlertsButton)
                    .addComponent(DeleteAllAlertsButton))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Alerts", alertsLabel);

        feedbackLabel.setBackground(new java.awt.Color(255, 255, 255));

        ResolvedIssues.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9" }));
        ResolvedIssues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResolvedIssuesActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel6.setText("Resolved issues:");

        jLabel7.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel7.setText("Subject:");

        jLabel8.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel8.setText("Description:");

        ResolvedByLabel.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        ResolvedByLabel.setText("Resolved by:");

        SendFeedbackButton.setText("Send Feedback");
        SendFeedbackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendFeedbackButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(DescriptionPane);

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
                    .addComponent(SendFeedbackButton)
                    .addGroup(feedbackLabelLayout.createSequentialGroup()
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ErrorFeedback)
                            .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(feedbackLabelLayout.createSequentialGroup()
                                    .addComponent(ResolvedIssues, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(ResolvedByLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(AdminName))
                                .addComponent(SubjectPane)
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
                                    .addComponent(starButton5))))))
                .addContainerGap(121, Short.MAX_VALUE))
        );
        feedbackLabelLayout.setVerticalGroup(
            feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackLabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ResolvedIssues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(ResolvedByLabel)
                    .addComponent(AdminName))
                .addGap(12, 12, 12)
                .addGroup(feedbackLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SubjectPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(SendFeedbackButton)
                .addGap(18, 18, 18)
                .addComponent(ErrorFeedback)
                .addContainerGap(111, Short.MAX_VALUE))
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

    private void feedSearchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSearchTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSearchTextFieldActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        // TODO add your handling code here:
        //get database up to date
        int selectedIndex = issueViewList.getSelectedIndex();
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    int currentPriority = issue.getPriority();
                    currentPriority++;
                    issue.setPriority(currentPriority);
                    DatabaseLogic.updateWaypoint(issue);
                    this.upButton.setEnabled(false);
                    this.downButton.setEnabled(true);
                    this.priorityLabel.setText(String.valueOf(issue.getPriority()));
                    break;
                }
            }
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_reportButtonActionPerformed

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        // TODO add your handling code here:
        int selectedIndex = issueViewList.getSelectedIndex();
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    this.mapViewer.setAddressLocation(new GeoPosition(issue.getLatitude(), issue.getLongitude()));
                    this.mapViewer.setZoom(2);
                    break;
                }
            }
        }
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_viewButtonActionPerformed

    private void commentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentsButtonActionPerformed
        // TODO add your handling code here:
        CommentUserFrame commentObj = new CommentUserFrame();
        System.out.println("Name of the user: "+currentUserData.getName()+" "+currentUserData.getId());
        commentObj.setLoggedInUser(currentUserData);
        int index = issueViewList.getSelectedIndex();
        IssueData selectedIssue=issueDataList.get(index);
        System.out.println(selectedIssue.getTitle());
        commentObj.setCurrentIssue(selectedIssue);
        commentObj.loadComments(selectedIssue);
        //commentObj.setCurrentIssue(selectedIssue);
        commentObj.show();
    }//GEN-LAST:event_commentsButtonActionPerformed

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
        rating=1;
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
        rating=2;
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
        rating=3;
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
        rating=4;
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
        rating=5;
    }//GEN-LAST:event_starButton5ActionPerformed

    private void logOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logOutButtonActionPerformed
        //la log out trebuie salvat in baza de date toate update-urile issue-urilor (in acest caz suntem la user, deci doar prioritatea unui issue a fost modificata)
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
       int selectedIndex = issueViewList.getSelectedIndex();
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    photoViewObj.setImage(issue.getImage());
                    break;
                }
            }
        }
       photoViewObj.show();
    }//GEN-LAST:event_photoLabelMouseClicked

    private void mapPanelSecondaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelSecondaryMouseClicked
  
    }//GEN-LAST:event_mapPanelSecondaryMouseClicked

    private void mapSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSearchButtonActionPerformed
        // TODO add your handling code here:
        String searchItem = searchMapTextPane.getText().replace(" ", "%20") + "%20";
        GeoPosition searchCoord = searchLocation(searchItem);
        mapViewer.setZoom(1);
        mapViewer.setAddressLocation(searchCoord);
    }//GEN-LAST:event_mapSearchButtonActionPerformed

    private void issueViewListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_issueViewListValueChanged
        
        int selectedIndex = issueViewList.getSelectedIndex();
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    this.titleLabel.setText(issue.getTitle());
                    this.descriptionEditorPane.setText(issue.getDescription());
                    Image scaledImage = issue.getImage().getImage().getScaledInstance(photoLabel.getWidth(), photoLabel.getHeight(), Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    this.photoLabel.setIcon(scaledIcon);
                    this.cityLabel.setText(issue.getCity());
                    this.addressEditorPane.setText(issue.getAddress());
                    this.priorityLabel.setText(String.valueOf(issue.getPriority()));
                    this.userLabel.setText(issue.getUsername());
                    this.dateLabel.setText(issue.getDate().toString());
                    this.statusLabel.setText(issue.getStatus());
                    break;
                }
            }
        }
    }//GEN-LAST:event_issueViewListValueChanged

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        // TODO add your handling code here:
         int selectedIndex = issueViewList.getSelectedIndex();
        if(selectedIndex != -1)
        {
            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
            for(IssueData issue : issueDataList)
            {
                if(issue.getTitle().equals(selectedTitle))
                {
                    int currentPriority = issue.getPriority();
                    currentPriority--;
                    issue.setPriority(currentPriority);
                    DatabaseLogic.updateWaypoint(issue);
                    this.upButton.setEnabled(true);
                    this.downButton.setEnabled(false);
                    this.priorityLabel.setText(String.valueOf(issue.getPriority()));
                    break;
                }
            }
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void feedSearchTextFieldInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_feedSearchTextFieldInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSearchTextFieldInputMethodTextChanged

    private void feedSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSearchButtonActionPerformed
        // TODO add your handling code here:
        for(int i = 0; i < issueViewListModel.getSize(); i++)
        {
            if(feedSearchTextField.getText().equals(issueViewListModel.getElementAt(i)))
            {
                issueViewList.setSelectedIndex(i);
                break;
            }
        }
    }//GEN-LAST:event_feedSearchButtonActionPerformed

    private void DeleteAlertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteAlertsButtonActionPerformed
        // TODO add your handling code here:
        int selectedIndex=AlertList.getSelectedIndex();
        
        DatabaseLogic.deleteAlert(currentUserData, alerts.get(selectedIndex));
        alerts.remove(AlertList.getSelectedIndex());
        alertsViewListModel.removeElementAt(AlertList.getSelectedIndex());
        initAlerts(currentUserData);
    }//GEN-LAST:event_DeleteAlertsButtonActionPerformed

    private void DeleteAllAlertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteAllAlertsButtonActionPerformed
        // TODO add your handling code here:
        DatabaseLogic.deleteAllAlerts(currentUserData);
        initAlerts(currentUserData);
    }//GEN-LAST:event_DeleteAllAlertsButtonActionPerformed

    private void ResolvedIssuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResolvedIssuesActionPerformed
        // TODO add your handling code here:
        int selectedIndex=ResolvedIssues.getSelectedIndex();
        SolvedIssues issue=solvedIssues.get(selectedIndex);
        admin=DatabaseLogic.getUserById(issue.getUserId());
        AdminName.setText(admin.getName());
    }//GEN-LAST:event_ResolvedIssuesActionPerformed

    private void SendFeedbackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendFeedbackButtonActionPerformed
        // TODO add your handling code here:
        if(solvedIssues.size()>0 && rating>0)
        {
            Feedback feedback=new Feedback();
            int selectedIndex=ResolvedIssues.getSelectedIndex();
            feedback.setSolvedId(solvedIssues.get(selectedIndex).getId());
            feedback.setUserId(currentUserData.getId());
            feedback.setSubject(SubjectPane.getText());
            feedback.setRating(rating);
            feedback.setDescription(DescriptionPane.getText());
            String errorCheck=DatabaseLogic.addFeedback(feedback);
            if(errorCheck!=null)
            {
                ErrorFeedback.setText(errorCheck);
            }
            else
            {
                ErrorFeedback.setText("");
                
                String issue;
                issue=DatabaseLogic.getIssuebyId(solvedIssues.get(selectedIndex));
                String alertText="Recieved feedback for issue: "+issue+", rating: "+rating;
                if(DescriptionPane.getText()!=null)
                {
                    alertText+=", description: "+DescriptionPane.getText();
                }
                alert.setText(alertText);
                DatabaseLogic.addAlertForAdmin(alert, admin);
                
            }
            SubjectPane.setText("");
            DescriptionPane.setText("");
        }
        else
        {
            ErrorFeedback.setText("Cannot give feedback: Issue and rating requiered");
        }
        
    }//GEN-LAST:event_SendFeedbackButtonActionPerformed
    
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
    private javax.swing.JLabel AdminName;
    private javax.swing.JList<String> AlertList;
    private javax.swing.JButton DeleteAlertsButton;
    private javax.swing.JButton DeleteAllAlertsButton;
    private javax.swing.JEditorPane DescriptionPane;
    private javax.swing.JLabel ErrorFeedback;
    private javax.swing.JLabel ResolvedByLabel;
    private javax.swing.JComboBox<String> ResolvedIssues;
    private javax.swing.JButton SendFeedbackButton;
    private javax.swing.JTextField SubjectPane;
    private javax.swing.JEditorPane addressEditorPane;
    private javax.swing.JButton alertsButton;
    private javax.swing.JPanel alertsLabel;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JComboBox<String> comboMapType;
    private javax.swing.JButton commentsButton;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JEditorPane descriptionEditorPane;
    private javax.swing.JButton downButton;
    private javax.swing.JButton feedButton;
    private javax.swing.JPanel feedPanel;
    private javax.swing.JButton feedSearchButton;
    private javax.swing.JTextField feedSearchTextField;
    private javax.swing.JButton feedbackButton;
    private javax.swing.JPanel feedbackLabel;
    private javax.swing.JList<String> issueViewList;
    private javax.swing.JPanel issueViewPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton logOutButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JButton mapButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JPanel mapPanelSecondary;
    private javax.swing.JButton mapSearchButton;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JButton reportButton;
    private javax.swing.JTextPane searchMapTextPane;
    private javax.swing.JButton starButton1;
    private javax.swing.JButton starButton2;
    private javax.swing.JButton starButton3;
    private javax.swing.JButton starButton4;
    private javax.swing.JButton starButton5;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton upButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables

}
