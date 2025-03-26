package Client;

import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterForm extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JButton registerButton;

    private static final Color BACKGROUND_COLOR = new Color(32, 32, 32); // Dark Gray
    private static final Color PRIMARY_COLOR = new Color(150, 86, 248);  // Green Accent
    private static final Color SECONDARY_COLOR = new Color(44, 44, 44); // Darker Gray
    private static final Color TEXT_COLOR = new Color(220, 220, 220); // Light Gray

    public RegisterForm() {
        setTitle("Register - DocSync");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        add(nameLabel, gbc);

        nameField = new JTextField(20);
        nameField.setBackground(SECONDARY_COLOR);
        nameField.setForeground(TEXT_COLOR);
        nameField.setCaretColor(TEXT_COLOR);
        gbc.gridx = 1;
        add(nameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 1;
        add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setBackground(SECONDARY_COLOR);
        emailField.setForeground(TEXT_COLOR);
        emailField.setCaretColor(TEXT_COLOR);
        gbc.gridx = 1;
        add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setBackground(SECONDARY_COLOR);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        gbc.gridx = 1;
        add(passwordField, gbc);

        registerButton = new JButton("Register");
        registerButton.setBackground(PRIMARY_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(registerButton, gbc);

        JLabel loginLabel = new JLabel("Already have an account? ");
        loginLabel.setForeground(TEXT_COLOR);
        JButton loginLink = new JButton("Login here");
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setForeground(PRIMARY_COLOR);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(BACKGROUND_COLOR);
        loginPanel.add(loginLabel);
        loginPanel.add(loginLink);
        gbc.gridy = 4;
        add(loginPanel, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
    }

    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // TODO: Send register request to server
        JOptionPane.showMessageDialog(this, "Registration clicked for " + name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            new RegisterForm().setVisible(true);
        });
    }
}
