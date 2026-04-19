package eventsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * LoginFrame – the application entry point screen.
 */
public class LoginFrame extends JFrame {

    // ── UI components ─────────────────────────────────────────
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblError;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
    setTitle("Event Management System – Login");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(420, 340);
    setLocationRelativeTo(null);
    setResizable(false);
    setLayout(null);
    getContentPane().setBackground(new Color(224, 224, 224)); // any RGB color

    // ── Header panel ──────────────────────────────────────
    JPanel headerPanel = new JPanel(null);
    headerPanel.setBackground(new Color(4, 149, 15));
    headerPanel.setBounds(0, 0, 420, 80);
    add(headerPanel);

    // ── Logo (only once, on the left) ─────────────────────
  try {
   String logoPath = "C:/Users/roxel john/Documents/NetBeansProjects/EventSystem/src/images/cropped-CCS.png";
    java.io.File imgFile = new java.io.File(logoPath);
    System.out.println("Looking for logo at: " + imgFile.getAbsolutePath());

    if (imgFile.exists()) {
        Image scaledImage = new ImageIcon(imgFile.getAbsolutePath())
                .getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
        lblLogo.setBounds(10, 12, 55, 55);
        headerPanel.add(lblLogo);
        System.out.println("Logo loaded successfully!");
    } else {
        System.out.println("File does not exist at: " + imgFile.getAbsolutePath());
    }
} catch (Exception ex) {
    System.err.println("Logo load error: " + ex.getMessage());
}

    // ── Title & subtitle (shifted right to make room for logo) ─
    JLabel lblTitle = new JLabel("Event Management System");
    lblTitle.setFont(new Font("Franklin Gothic Medium", Font.BOLD, 16));
    lblTitle.setForeground(Color.WHITE);
    lblTitle.setBounds(75, 15, 330, 25);
    headerPanel.add(lblTitle);

    JLabel lblSub = new JLabel("Please log in to continue");
    lblSub.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 12));
    lblSub.setForeground(new Color(200, 220, 255));
    lblSub.setBounds(75, 43, 330, 18);
    headerPanel.add(lblSub);

    // ── Form ──────────────────────────────────────────────
    JLabel lblUser = new JLabel("Username:");
    lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblUser.setBounds(50, 105, 100, 25);
    add(lblUser);

    txtUsername = new JTextField();
    txtUsername.setBounds(155, 105, 200, 28);
    txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    add(txtUsername);

    JLabel lblPass = new JLabel("Password:");
    lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblPass.setBounds(50, 148, 100, 25);
    add(lblPass);

    txtPassword = new JPasswordField();
    txtPassword.setBounds(155, 148, 200, 28);
    txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    add(txtPassword);

    lblError = new JLabel("");
    lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblError.setForeground(Color.RED);
    lblError.setBounds(50, 185, 320, 20);
    add(lblError);

    btnLogin = new JButton("Login");
    btnLogin.setBounds(155, 215, 200, 35);
    btnLogin.setBackground(new Color(30, 80, 160));
    btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnLogin.setForeground(Color.BLACK);
    btnLogin.setFocusPainted(false);
    btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
    add(btnLogin);

    // ── Actions ───────────────────────────────────────────
    btnLogin.addActionListener(e -> doLogin());
    txtPassword.addActionListener(e -> doLogin());

    // ── Footer ────────────────────────────────────────────
    JLabel lblFooter = new JLabel("Gordon College – College of Computer Studies");
    lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 10));
    lblFooter.setForeground(Color.BLACK);
    lblFooter.setBounds(100, 285, 300, 15);
    add(lblFooter);
}

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        // ── Validation: empty fields ──────────────────────────
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Both username and password are required.");
            return;
        }

        String hashedPassword = PasswordUtil.hash(password);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT user_id, full_name FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int    userId   = rs.getInt("user_id");
                String fullName = rs.getString("full_name");

                SessionManager.login(userId, username, fullName);

                // Open Dashboard, close Login
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
                this.dispose();
            } else {
                lblError.setText("Invalid username or password.");
                txtPassword.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Entry point ───────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
