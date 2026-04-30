
package eventandattendeemanagementsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;

/**
 * RegistrationFrame – full CRUD for attendee registrations.
 * Includes slot enforcement and duplicate email prevention.
 */
public class RegistrationFrame extends JFrame {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboEvents;
    private int[]             eventIds;

    // ── Form fields ───────────────────────────────────────────
    private JTextField        txtFirstName, txtLastName, txtEmail, txtContact;
    private JComboBox<String> cboEventForm;
    private int[]             formEventIds;

    private JButton btnRegister, btnUpdate, btnDelete, btnClear, btnBack;
    private JLabel  lblSlotsInfo;

    private int selectedRegId = -1;

    public RegistrationFrame() {
        initComponents();
        loadEventCombos();
        loadRegistrations();
    }

    private void initComponents() {
        setTitle("Attendee Registration – Event Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { goBack(); }
        });

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(null);
        header.setBackground(new Color(46, 204, 113));
        header.setBounds(0, 0, 1050, 60);
        add(header);

        JLabel lblTitle = new JLabel("Attendee Registration Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 15, 500, 30);
        header.add(lblTitle);

        btnBack = new JButton("← Back to Dashboard");
        btnBack.setBounds(860, 15, 170, 30);
        btnBack.setBackground(new Color(255, 255, 255, 50));
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> goBack());
        header.add(btnBack);

        // ── Left panel: Form ──────────────────────────────────
        JPanel formPanel = new JPanel(null);
        formPanel.setBorder(BorderFactory.createTitledBorder("Register Attendee"));
        formPanel.setBounds(10, 70, 340, 535);
        add(formPanel);

        int fy = 25;
        addFLabel(formPanel, "Event:", 10, fy);
        cboEventForm = new JComboBox<>();
        cboEventForm.setBounds(10, fy + 20, 315, 28);
        cboEventForm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cboEventForm);
        cboEventForm.addActionListener(e -> showSlotInfo());
        fy += 65;

        lblSlotsInfo = new JLabel("Select an event to see slot info");
        lblSlotsInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSlotsInfo.setForeground(new Color(52, 73, 94));
        lblSlotsInfo.setBounds(10, fy, 315, 20);
        formPanel.add(lblSlotsInfo);
        fy += 30;

        addFLabel(formPanel, "First Name:", 10, fy);
        txtFirstName = addFTextField(formPanel, 10, fy + 20);
        fy += 63;

        addFLabel(formPanel, "Last Name:", 10, fy);
        txtLastName = addFTextField(formPanel, 10, fy + 20);
        fy += 63;

        addFLabel(formPanel, "Email Address:", 10, fy);
        txtEmail = addFTextField(formPanel, 10, fy + 20);
        fy += 63;

        addFLabel(formPanel, "Contact Number (11 digits):", 10, fy);
        txtContact = addFTextField(formPanel, 10, fy + 20);
        fy += 73;

        btnRegister = fButton("Register",      new Color(46, 204, 113));
        btnUpdate   = fButton("Update",        new Color(52, 152, 219));
        btnDelete   = fButton("Remove",        new Color(231, 76, 60));
        btnClear    = fButton("Clear",         new Color(149, 165, 166));

        btnRegister.setBounds(10,  fy, 145, 35); formPanel.add(btnRegister);
        btnUpdate.setBounds(170,   fy, 145, 35); formPanel.add(btnUpdate);
        fy += 45;
        btnDelete.setBounds(10,    fy, 145, 35); formPanel.add(btnDelete);
        btnClear.setBounds(170,    fy, 145, 35); formPanel.add(btnClear);

        btnRegister.addActionListener(e -> registerAttendee());
        btnUpdate.addActionListener(e   -> updateAttendee());
        btnDelete.addActionListener(e   -> deleteRegistration());
        btnClear.addActionListener(e    -> clearForm());

        // ── Right panel: Filter + Table ───────────────────────
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Registrations"));
        rightPanel.setBounds(360, 70, 675, 535);
        add(rightPanel);

        JLabel lblFilter = new JLabel("Filter by Event:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFilter.setBounds(10, 20, 120, 22);
        rightPanel.add(lblFilter);

        cboEvents = new JComboBox<>();
        cboEvents.setBounds(135, 20, 350, 26);
        cboEvents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightPanel.add(cboEvents);

        JButton btnFilter = new JButton("Filter");
        btnFilter.setBounds(495, 18, 80, 28);
        btnFilter.addActionListener(e -> loadRegistrations());
        rightPanel.add(btnFilter);

        JButton btnAll = new JButton("Show All");
        btnAll.setBounds(580, 18, 85, 28);
        btnAll.addActionListener(e -> { cboEvents.setSelectedIndex(0); loadRegistrations(); });
        rightPanel.add(btnAll);

        String[] cols = {"ID","Full Name","Email","Contact","Event","Registered At","Attendance"};
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(10, 55, 650, 465);
        rightPanel.add(scroll);
    }

    // ── Load event combos ─────────────────────────────────────
    private void loadEventCombos() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT event_id, event_name FROM events ORDER BY event_date");

            java.util.List<String> names = new java.util.ArrayList<>();
            java.util.List<Integer> ids  = new java.util.ArrayList<>();

            names.add("-- All Events --"); ids.add(-1);
            cboEvents.addItem("-- All Events --");

            cboEventForm.removeAllItems();
            java.util.List<String> formNames = new java.util.ArrayList<>();
            java.util.List<Integer> formIds  = new java.util.ArrayList<>();

            while (rs.next()) {
                String label = rs.getString("event_name");
                int    id    = rs.getInt("event_id");
                cboEvents.addItem(label);
                ids.add(id);
                cboEventForm.addItem(label);
                formNames.add(label);
                formIds.add(id);
            }

            eventIds    = ids.stream().mapToInt(i -> i).toArray();
            formEventIds = formIds.stream().mapToInt(i -> i).toArray();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
    }

    private void showSlotInfo() {
        int idx = cboEventForm.getSelectedIndex();
        if (idx < 0 || formEventIds == null || idx >= formEventIds.length) return;
        int eid = formEventIds[idx];
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT max_slots, (SELECT COUNT(*) FROM registrations WHERE event_id=?) AS reg FROM events WHERE event_id=?");
            stmt.setInt(1, eid); stmt.setInt(2, eid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int max  = rs.getInt("max_slots");
                int reg  = rs.getInt("reg");
                int rem  = max - reg;
                lblSlotsInfo.setForeground(rem == 0 ? Color.RED : new Color(39, 174, 96));
                lblSlotsInfo.setText("Slots: " + reg + " registered / " + max + " max (" + rem + " remaining)");
            }
        } catch (SQLException ex) { lblSlotsInfo.setText("Could not load slot info"); }
    }

    // ── Load registrations table ──────────────────────────────
    void loadRegistrations() {
        tableModel.setRowCount(0);
        int selectedIdx = cboEvents.getSelectedIndex();
        int filterEventId = (eventIds != null && selectedIdx > 0) ? eventIds[selectedIdx] : -1;

        String sql = "SELECT r.registration_id, r.first_name, r.last_name, r.email, r.contact_number, " +
            "e.event_name, r.registered_at, r.attendance_status " +
            "FROM registrations r JOIN events e ON r.event_id = e.event_id " +
            (filterEventId > 0 ? "WHERE r.event_id = ? " : "") +
            "ORDER BY r.registered_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filterEventId > 0) stmt.setInt(1, filterEventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("registration_id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("contact_number"),
                    rs.getString("event_name"),
                    rs.getString("registered_at"),
                    rs.getString("attendance_status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading registrations: " + ex.getMessage());
        }
    }

    // ── Populate form ─────────────────────────────────────────
    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedRegId = (int) tableModel.getValueAt(row, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM registrations WHERE registration_id = ?");
            stmt.setInt(1, selectedRegId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtFirstName.setText(rs.getString("first_name"));
                txtLastName.setText(rs.getString("last_name"));
                txtEmail.setText(rs.getString("email"));
                txtContact.setText(rs.getString("contact_number"));
                int eid = rs.getInt("event_id");
                // Select matching event in combo
                if (formEventIds != null) {
                    for (int i = 0; i < formEventIds.length; i++) {
                        if (formEventIds[i] == eid) { cboEventForm.setSelectedIndex(i); break; }
                    }
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ── CRUD ─────────────────────────────────────────────────
    private void registerAttendee() {
        if (!validateForm()) return;
        int eid = formEventIds[cboEventForm.getSelectedIndex()];

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Slot check — query at time of save
            PreparedStatement slotCheck = conn.prepareStatement(
                "SELECT max_slots, (SELECT COUNT(*) FROM registrations WHERE event_id=?) AS reg FROM events WHERE event_id=?");
            slotCheck.setInt(1, eid); slotCheck.setInt(2, eid);
            ResultSet rs1 = slotCheck.executeQuery();
            if (rs1.next() && rs1.getInt("reg") >= rs1.getInt("max_slots")) {
                JOptionPane.showMessageDialog(this,
                    "This event is already at full capacity. Registration rejected.",
                    "Event Full", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Duplicate email check
            PreparedStatement dupCheck = conn.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE event_id=? AND email=?");
            dupCheck.setInt(1, eid); dupCheck.setString(2, txtEmail.getText().trim());
            ResultSet rs2 = dupCheck.executeQuery();
            if (rs2.next() && rs2.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                    "This email is already registered for the selected event.",
                    "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Insert
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO registrations (event_id, first_name, last_name, email, contact_number) VALUES (?,?,?,?,?)");
            ins.setInt(1, eid);
            ins.setString(2, txtFirstName.getText().trim());
            ins.setString(3, txtLastName.getText().trim());
            ins.setString(4, txtEmail.getText().trim());
            ins.setString(5, txtContact.getText().trim());
            ins.executeUpdate();

            JOptionPane.showMessageDialog(this, "Attendee registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadRegistrations();
            showSlotInfo();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateAttendee() {
        if (selectedRegId < 0) { JOptionPane.showMessageDialog(this, "Select a registration to update."); return; }
        if (!validateForm()) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE registrations SET first_name=?, last_name=?, email=?, contact_number=? WHERE registration_id=?")) {
            stmt.setString(1, txtFirstName.getText().trim());
            stmt.setString(2, txtLastName.getText().trim());
            stmt.setString(3, txtEmail.getText().trim());
            stmt.setString(4, txtContact.getText().trim());
            stmt.setInt(5, selectedRegId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Attendee updated.", "Updated", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadRegistrations();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteRegistration() {
        if (selectedRegId < 0) { JOptionPane.showMessageDialog(this, "Select a registration to remove."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove this registrant?",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM registrations WHERE registration_id=?")) {
            stmt.setInt(1, selectedRegId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registrant removed.", "Done", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadRegistrations(); showSlotInfo();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ── Validation ────────────────────────────────────────────
    private boolean validateForm() {
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()
                || txtEmail.getText().trim().isEmpty() || txtContact.getText().trim().isEmpty()
                || cboEventForm.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Pattern.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$", txtEmail.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!txtContact.getText().trim().matches("\\d{11}")) {
            JOptionPane.showMessageDialog(this, "Contact number must be exactly 11 digits.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedRegId = -1;
        txtFirstName.setText(""); txtLastName.setText("");
        txtEmail.setText(""); txtContact.setText("");
        if (cboEventForm.getItemCount() > 0) cboEventForm.setSelectedIndex(0);
        table.clearSelection();
        showSlotInfo();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void addFLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setBounds(x, y, 260, 18); p.add(l);
    }

    private JTextField addFTextField(JPanel p, int x, int y) {
        JTextField t = new JTextField();
        t.setBounds(x, y, 315, 28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(t); return t;
    }

    private JButton fButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false); return btn;
    }

    private void goBack() {
        DashboardFrame dash = new DashboardFrame();
        dash.setVisible(true);
        this.dispose();
    }
}