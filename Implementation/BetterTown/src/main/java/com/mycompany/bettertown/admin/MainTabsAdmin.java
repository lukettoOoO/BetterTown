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
import java.awt.Image;
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
import org.json.JSONArray;
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
import javax.swing.DefaultListModel;

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
    private DefaultListModel<String> issueViewListModel; //this is necessary for displaying the title of issues in the issue list JList in the feed
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
        
       issueViewListModel = new DefaultListModel<>();
       issueViewList.setModel(issueViewListModel);
       
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

                        // Add to the issue list
                        issueDataList.add(issueData);
                        issueViewListModel.addElement(issueData.getTitle()); //add the title of the issue in the feed issue list
                        printCurrentIssues();

                        // Add a waypoint to the map
                        //each waypoint has the issueData attribute, this is crucial for finding it in the feed
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
        for(MyWaypoint d : waypoints)  //se sterg butoanele waypoint-urilor
        {
            mapViewer.remove(d.getButton());
        }
        waypoints.clear(); //se sterg waypint-urile
        initWaypoint(); //se reinitializeaza waypoint-urile pe harta pentru ca aceasta sa fie actualizata
    }
    
    public void removeWaypoint(MyWaypoint waypoint)
    {
            mapViewer.remove(waypoint.getButton());
            waypoints.remove(waypoint);
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
    
       private IssueData getIssueAtSelectedJListIndex()
    {
        int selectedIndex = issueViewList.getSelectedIndex();
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
        
        ImageIcon managerIcon = new ImageIcon("manager.png");
        managerButton.setIcon(managerIcon);
        
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
        jScrollPane6 = new javax.swing.JScrollPane();
        searchMapTextPane = new javax.swing.JTextPane();
        mapSearchButton = new javax.swing.JButton();
        feedPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        feedSearchTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        feedSortComboBox = new javax.swing.JComboBox<>();
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
        updateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
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

        searchMapTextPane.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 11)); // NOI18N
        searchMapTextPane.setToolTipText("");
        jScrollPane6.setViewportView(searchMapTextPane);

        mapSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSearchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mapPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
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
                .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(jLabel5))
                    .addGroup(mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mapSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapPanelSecondary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
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

        jLabel2.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 13)); // NOI18N
        jLabel2.setText("Sort by:");

        feedSortComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ascending", "Descending" }));
        feedSortComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedSortComboBoxActionPerformed(evt);
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

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        deleteButton.setForeground(new java.awt.Color(255, 0, 0));
        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(feedSortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(reportButton)
                .addGap(23, 23, 23))
            .addComponent(jSeparator1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedPanelLayout.createSequentialGroup()
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(feedPanelLayout.createSequentialGroup()
                                .addComponent(updateButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(feedPanelLayout.createSequentialGroup()
                                .addComponent(deleteButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(commentsButton)))
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
                        .addComponent(jLabel2)
                        .addComponent(feedSortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(reportButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issueViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(feedPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewButton)
                            .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(commentsButton)
                            .addComponent(deleteButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(feedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(upButton)
                            .addComponent(downButton))))
                .addGap(29, 29, 29))
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
                        .addGap(0, 500, Short.MAX_VALUE))
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

        jList3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 1, true), "Account List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(".AppleSystemUIFont", 0, 24), new java.awt.Color(51, 204, 255))); // NOI18N
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
                .addContainerGap(132, Short.MAX_VALUE))
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 528, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void logOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logOutButtonActionPerformed
        //la log out trebuie salvat in baza de date toate update-urile issue-urilor
        LogoutConfirmationFrame logoutObj = new LogoutConfirmationFrame(new LogoutListener() {
        @Override
        public void onLogoutConfirmed() {
            dispose(); // Close the main frame
        }
        });
        logoutObj.setVisible(true);
    }//GEN-LAST:event_logOutButtonActionPerformed

    private void mapPanelSecondaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelSecondaryMouseClicked

    }//GEN-LAST:event_mapPanelSecondaryMouseClicked

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

    private void mapSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSearchButtonActionPerformed
        // TODO add your handling code here:
        String searchItem = searchMapTextPane.getText().replace(" ", "%20") + "%20";
        GeoPosition searchCoord = searchLocation(searchItem);
        mapViewer.setZoom(1);
        mapViewer.setAddressLocation(searchCoord);
    }//GEN-LAST:event_mapSearchButtonActionPerformed

    private void feedSearchTextFieldInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_feedSearchTextFieldInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSearchTextFieldInputMethodTextChanged

    private void feedSearchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSearchTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSearchTextFieldActionPerformed

    private void feedSortComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSortComboBoxActionPerformed
        // TODO add your handling code here:
        int index = comboMapType.getSelectedIndex();
        switch (index) {
            case 0 -> {
                for (int i = 0; i < issueViewListModel.getSize() - 1; i++)
                {
                    for (int j = 0; j < issueViewListModel.getSize() - i - 1; j++)
                    {
                        if (issueViewListModel.getElementAt(j).compareTo(issueViewListModel.getElementAt(j + 1)) > 0)
                        {
                            String temp = issueViewListModel.getElementAt(j);
                            issueViewListModel.setElementAt(issueViewListModel.getElementAt(j + 1), j);
                            issueViewListModel.setElementAt(temp, j + 1);
                        }
                    }
                }
                issueViewList.setModel(issueViewListModel);
            }
            case 1 -> {
                for (int i = 0; i < issueViewListModel.getSize() - 1; i++)
                {
                    for (int j = 0; j < issueViewListModel.getSize() - i - 1; j++)
                    {
                        if (issueViewListModel.getElementAt(j).compareTo(issueViewListModel.getElementAt(j + 1)) < 0)
                        {
                            String temp = issueViewListModel.getElementAt(j);
                            issueViewListModel.setElementAt(issueViewListModel.getElementAt(j + 1), j);
                            issueViewListModel.setElementAt(temp, j + 1);
                        }
                    }
                }
                issueViewList.setModel(issueViewListModel);
            }
            default -> {
            }
        }
    }//GEN-LAST:event_feedSortComboBoxActionPerformed

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed
        // TODO add your handling code here:
        tabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_reportButtonActionPerformed

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

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
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
                    currentPriority++;
                    issue.setPriority(currentPriority);
                    this.upButton.setEnabled(false);
                    this.downButton.setEnabled(true);
                    this.priorityLabel.setText(String.valueOf(issue.getPriority()));
                    break;
                }
            }
        }
    }//GEN-LAST:event_upButtonActionPerformed

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
                    this.upButton.setEnabled(true);
                    this.downButton.setEnabled(false);
                    this.priorityLabel.setText(String.valueOf(issue.getPriority()));
                    break;
                }
            }
        }
    }//GEN-LAST:event_downButtonActionPerformed

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
        commentObj.show();
    }//GEN-LAST:event_commentsButtonActionPerformed

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

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // TODO add your handling code here:
        int selectedIndex = issueViewList.getSelectedIndex();
        UpdateIssue updateIssueForm = new UpdateIssue(new AddIssueListener() {
                    @Override
                    public void onIssueAdded(IssueData issueData) { //when confirm button has been pressed in update issue form
                        for(MyWaypoint w : waypoints)
                        {
                            if(w.getData().getTitle().equals(issueDataList.get(selectedIndex).getTitle()))
                            {
                               w.setData(issueData);
                               break;
                            }
                        }
                        initWaypoint();
                        issueDataList.set(selectedIndex, issueData);
                        issueViewListModel.setElementAt(issueData.getTitle(), selectedIndex);
                        printCurrentIssues();
                        if(selectedIndex != -1)
                        {
                            String selectedTitle = issueViewListModel.getElementAt(selectedIndex);
                            for(IssueData issue : issueDataList)
                            {
                                if(issue.getTitle().equals(selectedTitle))
                                {
                                    titleLabel.setText(issue.getTitle());
                                    descriptionEditorPane.setText(issue.getDescription());
                                    Image scaledImage = issue.getImage().getImage().getScaledInstance(photoLabel.getWidth(), photoLabel.getHeight(), Image.SCALE_SMOOTH);
                                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                                    photoLabel.setIcon(scaledIcon);
                                    cityLabel.setText(issue.getCity());
                                    addressEditorPane.setText(issue.getAddress());
                                    priorityLabel.setText(String.valueOf(issue.getPriority()));
                                    userLabel.setText(issue.getUsername());
                                    dateLabel.setText(issue.getDate().toString());
                                    statusLabel.setText(issue.getStatus());
                                    break;
                                }
                            }
                        }
                    }
                });
        IssueData selectedIssue = issueDataList.get(selectedIndex);
        updateIssueForm.setTitle(selectedIssue.getTitle());
        updateIssueForm.setDescription(selectedIssue.getDescription());
        updateIssueForm.setCity(selectedIssue.getCity());
        updateIssueForm.setAddress(selectedIssue.getAddress());
        updateIssueForm.setDate(selectedIssue.getDate());
        updateIssueForm.setPriority(selectedIssue.getPriority());
        updateIssueForm.setUserName(selectedIssue.getUsername());
        updateIssueForm.setStatus(selectedIssue.getStatus());
        updateIssueForm.setImage(selectedIssue.getImage());
        updateIssueForm.setLatitude(selectedIssue.getLatitude());
        updateIssueForm.setLongitude(selectedIssue.getLongitude());
        updateIssueForm.show();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

        for(MyWaypoint w : waypoints)
        {
            if(w.getData().getTitle().equals(issueDataList.get(issueViewList.getSelectedIndex()).getTitle()))
            {
                this.removeWaypoint(w);
                break;
            }
        }
        int selectedIndex = issueViewList.getSelectedIndex();
        issueDataList.remove(issueViewList.getSelectedIndex());
        issueViewListModel.removeElementAt(issueViewList.getSelectedIndex());
        initWaypoint();
        if(issueViewListModel.getSize() >= 1)
        {
            issueViewList.setSelectedIndex(selectedIndex - 1);
        }
        else if(issueViewListModel.getSize() == 0)
        {
            titleLabel.setText("");
            photoLabel.setIcon(null);
            cityLabel.setText("");
            priorityLabel.setText("");
            userLabel.setText("");
            dateLabel.setText("");
            statusLabel.setText("");
            addressEditorPane.setText("");
            descriptionEditorPane.setText("");
        }
        printCurrentIssues();
    }//GEN-LAST:event_deleteButtonActionPerformed
    
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
    private javax.swing.JEditorPane addressEditorPane;
    private javax.swing.JButton alertsButton;
    private javax.swing.JPanel alertsLabel;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JComboBox<String> comboMapType;
    private javax.swing.JButton commentsButton;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JEditorPane descriptionEditorPane;
    private javax.swing.JButton downButton;
    private javax.swing.JButton feedButton;
    private javax.swing.JPanel feedPanel;
    private javax.swing.JButton feedSearchButton;
    private javax.swing.JTextField feedSearchTextField;
    private javax.swing.JComboBox<String> feedSortComboBox;
    private javax.swing.JPanel feedbackLabel;
    private javax.swing.JList<String> issueViewList;
    private javax.swing.JPanel issueViewPanel;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jList3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JButton logOutButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JButton managerButton;
    private javax.swing.JButton mapButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JPanel mapPanelSecondary;
    private javax.swing.JButton mapSearchButton;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JButton reportButton;
    private javax.swing.JTextPane searchMapTextPane;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton upButton;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables

}
