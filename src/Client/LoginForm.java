package Client;

import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel logoLabel;

    private static final Color BACKGROUND_COLOR = new Color(32, 32, 32); // Dark Gray
    private static final Color PRIMARY_COLOR = new Color(150, 86, 248); // Purple Accent
    private static final Color SECONDARY_COLOR = new Color(44, 44, 44); // Darker Gray
    private static final Color TEXT_COLOR = new Color(220, 220, 220); // Light Gray

    public LoginForm() {
        setTitle("Login - DocSync");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        logoLabel = new JLabel(new ImageIcon("logo.png")); // Load the logo
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(logoLabel, gbc);

        JLabel emailLabel = createSmoothLabel("Email:");
        emailLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setBackground(SECONDARY_COLOR);
        emailField.setForeground(TEXT_COLOR);
        emailField.setCaretColor(TEXT_COLOR);
        gbc.gridx = 1;
        add(emailField, gbc);

        JLabel passwordLabel = createSmoothLabel("Password:");
        passwordLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setBackground(SECONDARY_COLOR);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        gbc.gridx = 1;
        add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginButton, gbc);

        JLabel registerLabel = new JLabel("Don't have an account? ");
        registerLabel.setForeground(TEXT_COLOR);
        JButton registerLink = new JButton("Create an account");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(PRIMARY_COLOR);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            new RegisterForm().setVisible(true);
            dispose();
        });

        JPanel registerPanel = new JPanel();
        registerPanel.setBackground(BACKGROUND_COLOR);
        registerPanel.add(registerLabel);
        registerPanel.add(registerLink);
        gbc.gridy = 4;
        add(registerPanel, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private JLabel createSmoothLabel(String text) {
        return new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                super.paintComponent(g);
            }
        };
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // TODO: Send login request to server
        JOptionPane.showMessageDialog(this, "Login clicked for " + email);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            new LoginForm().setVisible(true);
        });
    }
}