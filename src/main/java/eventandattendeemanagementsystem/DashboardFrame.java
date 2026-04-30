
package eventandattendeemanagementsystem;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardFrame extends JFrame {

    private JLabel lblWelcome;
    private JLabel lblTotalEvents, lblTotalRegs, lblFullEvents, lblUpcoming, lblError;

    public DashboardFrame() {
        if (!SessionManager.isLoggedIn()) {
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }
        initComponents();
        loadStats();
    }

    private void initComponents() {
        setTitle("Dashboard – Event Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(new Color(224, 224, 224));

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        // ── Header ─────────────────────────────────────────────
        JPanel header = new JPanel(null);
        header.setBackground(new Color(4, 149, 15));
        header.setBounds(0, 0, 700, 80);
        add(header);

        try {
            String logoPath = "C:/Users/roxel john/Documents/NetBeansProjects/EventSystem/src/images/cropped-CCS.png";
            java.io.File imgFile = new java.io.File(logoPath);
            if (imgFile.exists()) {
                Image scaledImage = new ImageIcon(imgFile.getAbsolutePath())
                        .getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
                lblLogo.setBounds(10, 12, 55, 55);
                header.add(lblLogo);
            }
        } catch (Exception ex) {
            System.err.println("Logo load error: " + ex.getMessage());
        }

        lblWelcome = new JLabel("Welcome, " + SessionManager.getFullName() + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(Color.YELLOW);
        lblWelcome.setBounds(75, 15, 500, 30);
        header.add(lblWelcome);

        JLabel lblSub = new JLabel("Event Registration and Attendee Management System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(75, 48, 500, 18);
        header.add(lblSub);

        // ── Stats section ───────────────────────────────────────
        JLabel lblStatsTitle = new JLabel("System Overview");
        lblStatsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatsTitle.setBounds(30, 100, 200, 25);
        add(lblStatsTitle);

        lblTotalEvents = createStatCard(30,  135, "Total Events",            "0", new Color(52, 152, 219));
        lblTotalRegs   = createStatCard(195, 135, "Total Registrations",     "0", new Color(46, 204, 113));
        lblFullEvents  = createStatCard(360, 135, "Events at Full Capacity", "0", new Color(231, 76, 60));
        lblUpcoming    = createStatCard(525, 135, "Upcoming Events",         "0", new Color(241, 196, 15));

        // ── Navigation ──────────────────────────────────────────
        JLabel lblNav = new JLabel("Navigation");
        lblNav.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNav.setBounds(30, 290, 200, 25);
        add(lblNav);

        JButton btnEvents = navButton("Manage Events", new Color(30, 80, 160));
        btnEvents.setBounds(30, 325, 180, 50);
        add(btnEvents);

        JButton btnRegs = navButton("Manage Registrations", new Color(46, 204, 113));
        btnRegs.setBounds(230, 325, 200, 50);
        add(btnRegs);

        JButton btnAttendance = navButton("Attendance Marking", new Color(241, 196, 15));
        btnAttendance.setBounds(445, 325, 200, 50);
        add(btnAttendance);

        JButton btnRefresh = navButton("Refresh Stats", new Color(149, 165, 166));
        btnRefresh.setBounds(30, 400, 150, 35);
        add(btnRefresh);

        JButton btnLogout = navButton("Logout", new Color(231, 76, 60));
        btnLogout.setBounds(495, 400, 150, 35);
        add(btnLogout);

        JLabel lblFooter = new JLabel("Gordon College – College of Computer Studies");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblFooter.setForeground(Color.BLACK);
        lblFooter.setBounds(240, 445, 300, 15);
        add(lblFooter);

        // ── Button Actions ──────────────────────────────────────
        btnEvents.addActionListener(e -> { new EventManagementFrame().setVisible(true); this.dispose(); });
        btnRegs.addActionListener(e -> { new RegistrationFrame().setVisible(true); this.dispose(); });
        btnAttendance.addActionListener(e -> { new AttendanceFrame().setVisible(true); this.dispose(); });
        btnRefresh.addActionListener(e -> loadStats());
        btnLogout.addActionListener(e -> doLogout());
    }

    private JLabel createStatCard(int x, int y, String title, String value, Color color) {
        RoundedPanel card = new RoundedPanel(30, color);
        card.setLayout(null);
        card.setBounds(x, y, 150, 130);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(card);

        JLabel lblTitle = new JLabel("<html><center>" + title + "</center></html>", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(5, 10, 140, 40);
        card.add(lblTitle);

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);
        lblValue.setBounds(5, 55, 140, 55);
        card.add(lblValue);

        return lblValue;
    }

    private JButton navButton(String text, Color bg) {
        RoundedButton btn = new RoundedButton(text, 25, bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM events");
            if (rs1.next()) lblTotalEvents.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM registrations");
            if (rs2.next()) lblTotalRegs.setText(String.valueOf(rs2.getInt(1)));

            String sqlFull = "SELECT COUNT(*) FROM events e WHERE " +
                "(SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.event_id) >= e.max_slots";
            ResultSet rs3 = conn.createStatement().executeQuery(sqlFull);
            if (rs3.next()) lblFullEvents.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM events WHERE event_date >= CURDATE()");
            if (rs4.next()) lblUpcoming.setText(String.valueOf(rs4.getInt(1)));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading stats: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── SINGLE RoundedPanel (with hover float effect) ───────────
    private class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;
        private boolean isHovered = false;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isHovered) {
                for (int i = 6; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 15 * i));
                    g2.fillRoundRect(i, i + 4, getWidth() - i * 2, getHeight() - i * 2, cornerRadius, cornerRadius);
                }
                g2.setColor(backgroundColor.brighter());
                g2.fillRoundRect(0, -3, getWidth(), getHeight(), cornerRadius, cornerRadius);
            } else {
                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── SINGLE RoundedButton ────────────────────────────────────
    private class RoundedButton extends JButton {
        private int radius;
        private Color backgroundColor;

        public RoundedButton(String text, int radius, Color bgColor) {
            super(text);
            this.radius = radius;
            this.backgroundColor = bgColor;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isArmed() ? backgroundColor.darker() : backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Exit the application?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection();
            System.exit(0);
        }
    }
}