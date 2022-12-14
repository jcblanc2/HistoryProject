package Views;

import Models.TreeNode;
import Models.SiteHistory;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

public abstract class HistoryView extends JFrame {

    // ------------------------------------ All variables -----------------------------------
    private JToolBar barre = new JToolBar();
    private JTextField searchView = new JTextField();
    private JScrollPane scrollTree;
    private static JTable table;
    private JScrollPane scrollTable;
    private JTree tree;
    private DefaultMutableTreeNode root;
    private static String  browserSelected = "";
    private static int row, column;
    private static ArrayList<SiteHistory> sites;


    private final JButton copy = new JButton("Copy", new ImageIcon("resources/Copy.png"));
    private final JButton refresh = new JButton("Refresh", new ImageIcon("resources/Refresh.png"));
    private final JButton sellectAll = new JButton("Select All", new ImageIcon("resources/SelectAll.png"));
    private final JButton sort = new JButton("Sort By", new ImageIcon("resources/Sort.png"));

    final String[] colHeads = {"Url", "Title", "Visit Time", "Visit Count", "User Profile"};
    String[][] data = {{"", "", "", "", ""}};
    private String OSName = System.getProperty("os.name"); // get the OS name

    private static final JPopupMenu sortList = new JPopupMenu("Popup Sort");
    private static final JRadioButton  Ascending = new JRadioButton ("Ascending");
    private static final JRadioButton  Descending = new JRadioButton ("Descending");


    // ------- Method to Search Chrome's history ------------
    public abstract void ChromeHistory(String choice) throws IOException, SQLException;


    // ------- Method to Search microsoftEdge's history ------------
    public abstract void microsoftEdgeHistory(String choice) throws IOException, SQLException;


    // ------- Method to Search Firefox's history ------------
    public abstract void firefoxHistory(String choice) throws IOException, SQLException;


    // ------- Method to Search Opera's history ------------
    public abstract void operaHistory(String choice) throws IOException, SQLException;


    // ------- Method to Search Vivaldi's history ------------
    public abstract void vivaldiHistory(String choice) throws IOException, SQLException;


    // ------- Method to Search Brave's history ------------
    public abstract void braveHistory(String choice) throws IOException, SQLException;


    // ------------------------------------------ The constructor -------------------------------------
    public HistoryView() {
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("History Explorer");

        Image icon = Toolkit.getDefaultToolkit().getImage("resources/History.png");
        setIconImage(icon); // Add icon

        Menu(); // Menu
        listRoot(); // List all browsers
        Display(); // Display the Tree and JTable
        buttonToolbarAction(); // When user click a button on the toolbar
        popUpSort(); // Display a popup to sort
        setVisible(true);
    }


    // ------------------------------ Menu and toolbar --------------------------------------------
    public void Menu() {
        barre.setFloatable(false);
        // put text on bottom of theses buttons
        copy.setVerticalTextPosition(JButton.BOTTOM);
        copy.setHorizontalTextPosition(JButton.CENTER);
        refresh.setVerticalTextPosition(JButton.BOTTOM);
        refresh.setHorizontalTextPosition(JButton.CENTER);

        sellectAll.setVerticalTextPosition(JButton.BOTTOM);
        sellectAll.setHorizontalTextPosition(JButton.CENTER);

        // add button to toolbar
        barre.add(refresh);
        barre.addSeparator(); // add a separator on the toolbar
        barre.add(copy);
        barre.addSeparator();
        barre.add(sellectAll);

        sort.setVerticalTextPosition(JButton.BOTTOM);
        sort.setHorizontalTextPosition(JButton.CENTER);

        barre.addSeparator();
        barre.add(sort);

        barre.addSeparator();
        searchView.setMaximumSize(new Dimension(200, 20)); // size for the searchView
        searchView.setText("Search by title"); // a default placeholder on the searchView
        barre.add(searchView);

        add(barre, BorderLayout.NORTH);
    }


    // ------------------------------------------- The GUI ---------------------------------------
    private void Display() {
        scrollTree = new JScrollPane(tree);

        final String[] colHeads = {"Url", "Title", "Visit Time", "Visit Count", "User Profile"};
        String[][] data = {{"", "", "", "", ""}};
        table = new JTable(data, colHeads);
        scrollTable = new JScrollPane(table);

        add(scrollTree, BorderLayout.WEST);
        add(scrollTable, BorderLayout.CENTER);


        // Click on tree
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // get the node selected
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

                // get value of the node selected by the user
                if (node.getUserObject() instanceof TreeNode){
                    TreeNode nodeSelected = (TreeNode) node.getUserObject();

                    try {
                        // call method "doMouseClicked"
                        doMouseClicked(nodeSelected.getValue());
                    } catch (IOException | SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        // Clear placeholder searchView
        searchView.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                searchView.setText("");
            }
        });


        // Search text in the database
        searchView.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                // check when user press Enter
                if (key == KeyEvent.VK_ENTER) {
                    Toolkit.getDefaultToolkit().beep();

                    // check if searchView is empty
                    if (searchView.getText().equals("")) {
                        try {
                            decision(browserSelected, "Display");
                            searchView.setText("Search by title");
                        } catch (IOException | SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        try {
                            // search the word
                            decision(browserSelected, searchView.getText().toString());
                            searchView.setText("Search by title");
                        } catch (IOException | SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                    }
                }
            }
        });
    }


    // ----------------------------------------- The Tree -------------------------------------------
    private void listRoot() {
        tree = new JTree(this.root);
        getContentPane().add(new JScrollPane(tree)); // tree's JScrollPane

        // root of the tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Browser");

        // Create all node
        DefaultMutableTreeNode chrome = new DefaultMutableTreeNode(new TreeNode("Chrome", "Chrome", "chrome.png"));
        DefaultMutableTreeNode microsoftEdge = new DefaultMutableTreeNode(new TreeNode("Microsoft Edge", "Microsoft Edge", "microsoft.png"));
        DefaultMutableTreeNode firefox = new DefaultMutableTreeNode(new TreeNode("Firefox", "Firefox", "firefox.png"));
        DefaultMutableTreeNode opera = new DefaultMutableTreeNode(new TreeNode("Opera", "Opera", "opera.png"));
        DefaultMutableTreeNode vivaldi = new DefaultMutableTreeNode(new TreeNode("Vivaldi", "Vivaldi", "vivaldi.png"));
        DefaultMutableTreeNode brave = new DefaultMutableTreeNode(new TreeNode("Brave", "Brave", "brave.png"));

        // Add node to the root
        root.add(chrome);
        root.add(microsoftEdge);
        root.add(firefox);
        root.add(opera);
        root.add(vivaldi);
        root.add(brave);


        DefaultTreeModel defaultTree = new DefaultTreeModel(root); // add root to DefaultTreeModel
        tree.setModel(defaultTree);
        tree.setCellRenderer(new nodeTreeCellRender());

        getContentPane().add(new JScrollPane(tree)); // tree's JScrollPane
    }


    // ------------------------------------------- Tree mouse event ---------------------------------------
    void doMouseClicked(String nodeSelected) throws IOException, SQLException {

        // Check click
        if (nodeSelected == null)
            return;

        browserSelected = nodeSelected;
        decision(browserSelected, "Display");
    }

    // Choice which the right browser
    public void decision(String browserSelected, String choice) throws IOException, SQLException {
        if (browserSelected.length() != 0) {
            switch (browserSelected.trim()) {
                case "Chrome" -> ChromeHistory(choice);
                case "Microsoft Edge" -> microsoftEdgeHistory(choice);
                case "Firefox" -> firefoxHistory(choice);
                case "Opera" -> operaHistory(choice);
                case "Vivaldi" -> vivaldiHistory(choice);
                case "Brave" -> braveHistory(choice);

            }
        }
    }

    // Get position mouse click on JTable
    void mousePosition(){
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                row = table.rowAtPoint(e.getPoint());
                column = table.columnAtPoint(e.getPoint());
            }
        });

    }


    // ------------------------------------------- Show data on JTable ---------------------------------------
    protected void showDetails(ArrayList<SiteHistory> listInfo) {

        sites = listInfo; // Take a copy of listInfo

        String[][] data = {{"", "", "", "", ""}};
        remove(scrollTable);

        table = new JTable(data, colHeads) {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                return false;
            }
        };

        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);

        scrollTable = new JScrollPane(table);
        add(scrollTable, BorderLayout.CENTER);
        setVisible(true);

        int fileCounter = 0;
        data = new String[listInfo.size()][5];

        for (SiteHistory details : listInfo) {
            data[fileCounter][0] = details.getUrl();
            data[fileCounter][1] = details.getTitle();
            data[fileCounter][2] = convertTime(details.getVisitTime());
            data[fileCounter][3] = String.valueOf(details.getVisitCount());
            data[fileCounter][4] = details.getUserProfile();

            fileCounter++;
        }

        String[][] dataTemp = new String[fileCounter][5];
        System.arraycopy(data, 0, dataTemp, 0, fileCounter);
        data = dataTemp;

        remove(scrollTable);

        table = new JTable(data, colHeads) {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                return false;
            }
        };

        // add mouse click when use select a row to display more details
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if(e.getClickCount() == 2){
                    new PopupDetail(); // Call popup detail
                    row = table.rowAtPoint(e.getPoint());
                    column = table.columnAtPoint(e.getPoint());

                    SiteHistory history = sites.get(row);
                    PopupDetail.setTxtTitle(history.getTitle());
                    PopupDetail.setTxtUrl(history.getUrl());
                    PopupDetail.setTxtTime(convertTime(history.getVisitTime()));
                    PopupDetail.setTxtVisitCount(String.valueOf(history.getVisitCount()));
                }
            }
        });


        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setCellSelectionEnabled(true);

        scrollTable = new JScrollPane(table);
        add(scrollTable, BorderLayout.CENTER);
        setVisible(true);
    }

    // Method to convert timeStamp
    public String convertTime(String date){
        // Getting the current system time and passing it and passing the long value in the Date class
        if(date != null){
            Timestamp ts = new Timestamp(Long.parseLong(date));
            Date newDate = new Date(ts.getTime());
            return String.valueOf(newDate);
        }else {
            return "None";
        }
    }


    //     Copy the database (To avoid an error like "database is locked")
    public void copyDatabase(String path) throws IOException, SQLException {
        File source = new File(path);
        File destination = null;

        if (OSName.contains("Windows")){
            destination = new File("windowsDatabase.sqlite");
            Files.deleteIfExists(Path.of("windowsDatabase.sqlite"));
            Files.copy(source.toPath(), destination.toPath());

        } else if (Objects.equals(OSName, "Linux")) {
            Files.deleteIfExists(Path.of("linuxDatabase.sqlite"));
            destination = new File("linuxDatabase.sqlite");
            Files.copy(source.toPath(), destination.toPath());
        }else {
            System.out.println("Other OS");
        }

    }


    //-------------------------- Popup for the Button Sort --------------------------------
    static void popUpSort() {
        JMenuItem Title = new JMenuItem("Title");
        JMenuItem Date = new JMenuItem("Date");
        JMenuItem vCount = new JMenuItem("Visit count");

        ButtonGroup group = new ButtonGroup();

        group.add(Ascending);
        group.add(Descending);

        // Sort by title
        Title.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sortBy("Title", Descending.isSelected());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        // Sort by date
        Date.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sortBy("Date", Descending.isSelected());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        // Sort by Visit count
        vCount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sortBy("Visit count", Descending.isSelected());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        // add item to the popup
        sortList.add(Title);
        sortList.add(Date);
        sortList.add(vCount);
        sortList.addSeparator();
        sortList.add(Ascending);
        sortList.add(Descending);
    }

    // Method to sort the data
    static void sortBy(String choice1, boolean choice2) throws IOException{
        int SortColNo = 0;

        // check if the user want to sort by title, date or Visit count
        switch(choice1) {
            case "Title" -> SortColNo = 1;
            case "Date" -> SortColNo = 2;
            case "Visit count" -> SortColNo = 3;
        }

        TableRowSorter<TableModel> ColSort = new TableRowSorter<>(table.getModel());
        table.setRowSorter(ColSort);
        ArrayList<RowSorter.SortKey> ColSortingKeys = new ArrayList<>();

        // check if user select ASCENDING or ASCENDING
        if ("true".equals(String.valueOf(choice2))) {
            ColSortingKeys.add(new RowSorter.SortKey(SortColNo, SortOrder.DESCENDING));
        } else {
            ColSortingKeys.add(new RowSorter.SortKey(SortColNo, SortOrder.ASCENDING));
        }

        ColSort.setSortKeys(ColSortingKeys);
        ColSort.sort();

        Descending.setSelected(false);
        Ascending.setSelected(true);
    }


    // When user click button on the toolbar
    void buttonToolbarAction(){

        // Button Sort action
        sort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortList.show(sort, sort.getWidth()/2-26, sort.getHeight()/2+30); // show popup
            }
        });


        // Button SelectAll action
        sellectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.selectAll();
            }
        });


        // Button copy (copy text)
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                mousePosition();

                String text = String.valueOf(table.getValueAt(row, column)); // get value at position mouse click
                StringSelection selection = new StringSelection(text); // copy the text
                clipboard.setContents(selection, null);
            }
        });


        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    decision(browserSelected, "Display");
                } catch (IOException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }


    private class nodeTreeCellRender extends DefaultTreeCellRenderer{
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.isLeaf()){
                TreeNode treeNode = (TreeNode) node.getUserObject();
                setText(treeNode.getValue());
                ImageIcon icon = new ImageIcon(new ImageIcon("resources/"+treeNode.getIcon()).getImage()
                        .getScaledInstance(24, 24, Image.SCALE_DEFAULT));
                setIcon(icon);
            }else {
                setLeafIcon(null);
                setClosedIcon(null);
                setOpenIcon(null);
            }

            return component;
        }
    }
}
