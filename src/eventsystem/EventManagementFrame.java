package eventsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * EventManagementFrame – full CRUD for events.
 * Status: Open / Full / Concluded
 */
public class EventManagementFrame extends JFrame {

    private JTable          table;
    private DefaultTableModel tableModel;
    private JTextField      txtSearch;

    // ── Form fields ───────────────────────────────────────────
    private JTextField      txtName, txtVenue, txtMaxSlots;
    private JTextArea       txtDescription;
    private JSpinner        spinDate;
    private JButton         btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    private int selectedEventId = -1;

    public EventManagementFrame() {
        initComponents();
        loadEvents();
    }

    private void initComponents() {
        setTitle("Event Management – Event Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { goBack(); }
        });

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(null);
        header.setBackground(new Color(30, 80, 160));
        header.setBounds(0, 0, 1000, 60);
        add(header);

        JLabel lblTitle = new JLabel("Event Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 15, 400, 30);
        header.add(lblTitle);

        btnBack = new JButton("← Back to Dashboard");
        btnBack.setBounds(820, 15, 160, 30);
        btnBack.setBackground(Color.BLACK);
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> goBack());
        header.add(btnBack);

        // ── Left panel: Form ──────────────────────────────────
        JPanel formPanel = new JPanel(null);
        formPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));
        formPanel.setBounds(10, 70, 320, 510);
        add(formPanel);

        int fy = 25;
        addLabel(formPanel, "Event Name:", 10, fy);
        txtName = addTextField(formPanel, 10, fy + 22);
        fy += 65;

        addLabel(formPanel, "Description:", 10, fy);
        txtDescription = new JTextArea();
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDescription.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBounds(10, fy + 22, 295, 70);
        formPanel.add(descScroll);
        fy += 110;

        addLabel(formPanel, "Event Date:", 10, fy);
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spinDate = new JSpinner(dateModel);
        spinDate.setEditor(new JSpinner.DateEditor(spinDate, "yyyy-MM-dd"));
        spinDate.setBounds(10, fy + 22, 295, 28);
        formPanel.add(spinDate);
        fy += 65;

        addLabel(formPanel, "Venue:", 10, fy);
        txtVenue = addTextField(formPanel, 10, fy + 22);
        fy += 65;

        addLabel(formPanel, "Maximum Slots:", 10, fy);
        txtMaxSlots = addTextField(formPanel, 10, fy + 22);
        fy += 70;

        btnAdd    = actionButton("Add Event",    new Color(46, 204, 113));
        btnUpdate = actionButton("Update Event", new Color(52, 152, 219));
        btnDelete = actionButton("Delete Event", new Color(231, 76, 60));
        btnClear  = actionButton("Clear Form",   new Color(149, 165, 166));

        btnAdd.setBounds(10,  fy,       140, 35); formPanel.add(btnAdd);
        btnUpdate.setBounds(165, fy,    140, 35); formPanel.add(btnUpdate);
        fy += 45;
        btnDelete.setBounds(10,  fy,    140, 35); formPanel.add(btnDelete);
        btnClear.setBounds(165,  fy,    140, 35); formPanel.add(btnClear);

        btnAdd.addActionListener(e    -> addEvent());
        btnUpdate.addActionListener(e -> updateEvent());
        btnDelete.addActionListener(e -> deleteEvent());
        btnClear.addActionListener(e  -> clearForm());

        // ── Right panel: Table ────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("All Events"));
        tablePanel.setBounds(340, 70, 648, 510);
        add(tablePanel);

        // Search bar
        JPanel searchBar = new JPanel(null);
        searchBar.setPreferredSize(new Dimension(640, 40));
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(5, 10, 55, 22);
        searchBar.add(lblSearch);
        txtSearch = new JTextField();
        txtSearch.setBounds(60, 10, 300, 22);
        searchBar.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(368, 8, 90, 26);
        btnSearch.addActionListener(e -> loadEvents());
        searchBar.add(btnSearch);
        JButton btnShowAll = new JButton("Show All");
        btnShowAll.setBounds(465, 8, 90, 26);
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadEvents(); });
        searchBar.add(btnShowAll);
        tablePanel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID","Event Name","Date","Venue","Max Slots","Registered","Remaining","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────
    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setBounds(x, y, 200, 18);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, int x, int y) {
        JTextField t = new JTextField();
        t.setBounds(x, y, 295, 28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(t);
        return t;
    }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        return btn;
    }

    // ── Load / Refresh table ──────────────────────────────────
    void loadEvents() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim();
        String sql = "SELECT e.event_id, e.event_name, e.event_date, e.venue, e.max_slots, " +
            "(SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.event_id) AS reg_count, " +
            "e.max_slots - (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.event_id) AS remaining " +
            "FROM events e " +
            (search.isEmpty() ? "" : "WHERE e.event_name LIKE ? OR e.venue LIKE ? ") +
            "ORDER BY e.event_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!search.isEmpty()) {
                stmt.setString(1, "%" + search + "%");
                stmt.setString(2, "%" + search + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int    id        = rs.getInt("event_id");
                String name      = rs.getString("event_name");
                String date      = rs.getString("event_date");
                String venue     = rs.getString("venue");
                int    maxSlots  = rs.getInt("max_slots");
                int    regCount  = rs.getInt("reg_count");
                int    remaining = rs.getInt("remaining");
                String status    = computeStatus(date, remaining);

                tableModel.addRow(new Object[]{id, name, date, venue, maxSlots, regCount, remaining, status});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String computeStatus(String dateStr, int remaining) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date eventDate = sdf.parse(dateStr);
            Date today     = new Date();
            // Normalize to date only
            SimpleDateFormat dayOnly = new SimpleDateFormat("yyyyMMdd");
            if (Integer.parseInt(dayOnly.format(eventDate)) < Integer.parseInt(dayOnly.format(today))) {
                return "Concluded";
            }
        } catch (Exception ignored) {}
        return remaining <= 0 ? "Full" : "Open";
    }

    // ── Populate form from selected row ───────────────────────
    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        selectedEventId = (int) tableModel.getValueAt(row, 0);
        String eventName = (String) tableModel.getValueAt(row, 1);
        String dateStr   = (String) tableModel.getValueAt(row, 2);
        String venue     = (String) tableModel.getValueAt(row, 3);
        int    maxSlots  = (int)    tableModel.getValueAt(row, 4);

        // Fetch description separately
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT description FROM events WHERE event_id = ?");
            stmt.setInt(1, selectedEventId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) txtDescription.setText(rs.getString("description"));
        } catch (SQLException ex) { txtDescription.setText(""); }

        txtName.setText(eventName);
        txtVenue.setText(venue);
        txtMaxSlots.setText(String.valueOf(maxSlots));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            spinDate.setValue(sdf.parse(dateStr));
        } catch (Exception ignored) {}
    }

    // ── CRUD Operations ───────────────────────────────────────
    private void addEvent() {
        if (!validateForm(true)) return;

        String sql = "INSERT INTO events (event_name, description, event_date, venue, max_slots) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtName.getText().trim());
            stmt.setString(2, txtDescription.getText().trim());
            stmt.setString(3, getSpinnerDate());
            stmt.setString(4, txtVenue.getText().trim());
            stmt.setInt(5, Integer.parseInt(txtMaxSlots.getText().trim()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEvent() {
        if (selectedEventId < 0) { JOptionPane.showMessageDialog(this, "Please select an event to update."); return; }
        if (!validateForm(false)) return;

        // Check if event has registrations – if so, do not allow date edit
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement chk = conn.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE event_id = ?");
            chk.setInt(1, selectedEventId);
            ResultSet rs = chk.executeQuery();
            int regCount = rs.next() ? rs.getInt(1) : 0;

            // Validate max_slots >= current registrations
            int newMax = Integer.parseInt(txtMaxSlots.getText().trim());
            if (newMax < regCount) {
                JOptionPane.showMessageDialog(this,
                    "Max slots cannot be less than current registrations (" + regCount + ").",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = regCount > 0
                ? "UPDATE events SET event_name=?, description=?, venue=?, max_slots=? WHERE event_id=?"
                : "UPDATE events SET event_name=?, description=?, event_date=?, venue=?, max_slots=? WHERE event_id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, txtName.getText().trim());
            stmt.setString(2, txtDescription.getText().trim());
            if (regCount > 0) {
                stmt.setString(3, txtVenue.getText().trim());
                stmt.setInt(4, newMax);
                stmt.setInt(5, selectedEventId);
            } else {
                stmt.setString(3, getSpinnerDate());
                stmt.setString(4, txtVenue.getText().trim());
                stmt.setInt(5, newMax);
                stmt.setInt(6, selectedEventId);
            }
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Event updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating event: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEvent() {
        if (selectedEventId < 0) { JOptionPane.showMessageDialog(this, "Please select an event to delete."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this event and ALL its registrations? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM events WHERE event_id = ?")) {
            stmt.setInt(1, selectedEventId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Event deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Validation ────────────────────────────────────────────
    private boolean validateForm(boolean checkFutureDate) {
        if (txtName.getText().trim().isEmpty() || txtVenue.getText().trim().isEmpty()
                || txtMaxSlots.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int slots = Integer.parseInt(txtMaxSlots.getText().trim());
            if (slots <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Maximum Slots must be a positive whole number.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (checkFutureDate) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String today = sdf.format(new Date());
                String picked = sdf.format((Date) spinDate.getValue());
                if (Integer.parseInt(picked) < Integer.parseInt(today)) {
                    JOptionPane.showMessageDialog(this, "Event Date must not be in the past.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (Exception ignored) {}
        }
        return true;
    }

    private String getSpinnerDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format((Date) spinDate.getValue());
    }

    void clearForm() {
        selectedEventId = -1;
        txtName.setText("");
        txtDescription.setText("");
        txtVenue.setText("");
        txtMaxSlots.setText("");
        spinDate.setValue(new Date());
        table.clearSelection();
    }

    private void goBack() {
        DashboardFrame dash = new DashboardFrame();
        dash.setVisible(true);
        this.dispose();
    }
}
