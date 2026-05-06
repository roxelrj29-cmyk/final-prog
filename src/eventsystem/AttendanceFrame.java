package eventsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * AttendanceFrame – mark attendance (Present / Absent / Pending) per event.
 * Shows a summary: total, present, absent, pending.
 */
public class AttendanceFrame extends JFrame {

    private JComboBox<String> cboEvents;
    private int[]             eventIds;

    private JTable            table;
    private DefaultTableModel tableModel;

    // Summary labels
    private JLabel lblTotal, lblPresent, lblAbsent, lblPending;

    private JButton btnBack;

    public AttendanceFrame() {
        initComponents();
        loadEvents();
    }

    private void initComponents() {
        setTitle("Attendance Marking – Event Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { goBack(); }
        });

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(null);
        header.setBackground(new Color(241, 196, 15));
        header.setBounds(0, 0, 900, 60);
        add(header);
        
        //logo----------------
        try {
            String logoPath = "C:/Users/roxel john/Documents/NetBeansProjects/EventSystem/src/images/cropped-CCS.png";
            java.io.File imgFile = new java.io.File(logoPath);
            if (imgFile.exists()) {
                Image scaledImage = new ImageIcon(imgFile.getAbsolutePath())
                        .getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
                lblLogo.setBounds(10, 3, 55, 55);
                header.add(lblLogo);
            }
        } catch (Exception ex) {
            System.err.println("Logo load error: " + ex.getMessage());
        }


        JLabel lblTitle = new JLabel("Attendance Marking");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBounds(75, 15, 400, 30);
        header.add(lblTitle);

        btnBack = new JButton("← Back to Dashboard");
        btnBack.setBounds(710, 15, 170, 30);
        btnBack.setBackground(new Color(255, 255, 255, 50));
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> goBack());
        header.add(btnBack);

        // ── Event selector ────────────────────────────────────
        JLabel lblSelectEvent = new JLabel("Select Event:");
        lblSelectEvent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSelectEvent.setBounds(20, 75, 110, 25);
        add(lblSelectEvent);

        cboEvents = new JComboBox<>();
        cboEvents.setBounds(135, 75, 400, 28);
        cboEvents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboEvents.addActionListener(e -> loadAttendance());
        add(cboEvents);

        // ── Summary cards ─────────────────────────────────────
        JPanel sumPanel = new JPanel(null);
        sumPanel.setBorder(BorderFactory.createTitledBorder("Attendance Summary"));
        sumPanel.setBounds(550, 65, 330, 130);
        add(sumPanel);

        lblTotal   = sumLabel(sumPanel, "Total Registrants:", 10, 25);
        lblPresent = sumLabel(sumPanel, "Present:",           10, 55);
        lblAbsent  = sumLabel(sumPanel, "Absent:",           10, 80);
        lblPending = sumLabel(sumPanel, "Pending:",          10, 105);

        // ── Table ─────────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Registrants"));
        tablePanel.setBounds(10, 210, 870, 330);
        add(tablePanel);

        String[] cols = {"Reg ID","Full Name","Email","Contact","Registered At","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Hide Reg ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Action buttons ────────────────────────────────────
        JPanel btnPanel = new JPanel(null);
        btnPanel.setBounds(10, 115, 530, 90);
        add(btnPanel);

        JLabel lblMark = new JLabel("Mark selected attendee as:");
        lblMark.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMark.setBounds(0, 5, 280, 20);
        btnPanel.add(lblMark);

        JButton btnPresent = markButton("✔ Present", new Color(46, 204, 113));
        btnPresent.setBounds(0, 35, 150, 40);
        btnPanel.add(btnPresent);

        JButton btnAbsent = markButton("✘ Absent", new Color(231, 76, 60));
        btnAbsent.setBounds(165, 35, 150, 40);
        btnPanel.add(btnAbsent);

        JButton btnPending = markButton("⏳ Pending", new Color(149, 165, 166));
        btnPending.setBounds(330, 35, 150, 40);
        btnPanel.add(btnPending);

        btnPresent.addActionListener(e -> markAttendance("Present"));
        btnAbsent.addActionListener(e  -> markAttendance("Absent"));
        btnPending.addActionListener(e -> markAttendance("Pending"));
    }

    private JLabel sumLabel(JPanel parent, String labelText, int x, int y) {
        JLabel lbl = new JLabel(labelText + " 0");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setBounds(x, y, 300, 18);
        parent.add(lbl);
        return lbl;
    }

    private JButton markButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        return btn;
    }

    // ── Load events into combo ────────────────────────────────
    private void loadEvents() {
        cboEvents.removeAllItems();
        cboEvents.addItem("-- Select an Event --");
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        ids.add(-1);
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                "SELECT event_id, event_name FROM events ORDER BY event_date")) {
            while (rs.next()) {
                cboEvents.addItem(rs.getString("event_name"));
                ids.add(rs.getInt("event_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        eventIds = ids.stream().mapToInt(i -> i).toArray();
    }

    // ── Load attendance for selected event ────────────────────
    private void loadAttendance() {
        tableModel.setRowCount(0);
        int idx = cboEvents.getSelectedIndex();
        if (idx <= 0 || eventIds == null || idx >= eventIds.length) {
            updateSummary(0, 0, 0, 0);
            return;
        }
        int eid = eventIds[idx];

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT registration_id, first_name, last_name, email, contact_number, registered_at, attendance_status " +
                "FROM registrations WHERE event_id = ? ORDER BY last_name, first_name");
            stmt.setInt(1, eid);
            ResultSet rs = stmt.executeQuery();
            int total = 0, present = 0, absent = 0, pending = 0;
            while (rs.next()) {
                String status = rs.getString("attendance_status");
                tableModel.addRow(new Object[]{
                    rs.getInt("registration_id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("contact_number"),
                    rs.getString("registered_at"),
                    status
                });
                total++;
                switch (status) {
                    case "Present": present++; break;
                    case "Absent":  absent++;  break;
                    default:        pending++; break;
                }
            }
            updateSummary(total, present, absent, pending);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateSummary(int total, int present, int absent, int pending) {
        lblTotal.setText("Total Registrants: " + total);
        lblPresent.setText("Present: " + present);
        lblAbsent.setText("Absent: " + absent);
        lblPending.setText("Pending: " + pending);
        lblPresent.setForeground(new Color(39, 174, 96));
        lblAbsent.setForeground(new Color(192, 57, 43));
        lblPending.setForeground(new Color(127, 140, 141));
    }

    // ── Mark attendance ───────────────────────────────────────
    private void markAttendance(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registrant first.");
            return;
        }
        int regId = (int) tableModel.getValueAt(row, 0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE registrations SET attendance_status = ? WHERE registration_id = ?")) {
            stmt.setString(1, status);
            stmt.setInt(2, regId);
            stmt.executeUpdate();
            loadAttendance(); // refresh table + summary
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating attendance: " + ex.getMessage());
        }
    }

    private void goBack() {
        new DashboardFrame().setVisible(true);
        this.dispose();
    }
}
