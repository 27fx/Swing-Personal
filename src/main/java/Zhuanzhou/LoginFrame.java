package Zhuanzhou;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("登录");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("用户名:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> login());
        this.add(loginButton);

        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String adminSql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        String userSql = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement adminStmt = conn.prepareStatement(adminSql);
             PreparedStatement userStmt = conn.prepareStatement(userSql)) {

            // Check admin credentials
            adminStmt.setString(1, username);
            adminStmt.setString(2, password);
            ResultSet adminRs = adminStmt.executeQuery();
            if (adminRs.next()) {
                int employeeId = adminRs.getInt("employee_id");
                openMainFrame("admin", employeeId);
                this.dispose();
                return;
            }


            userStmt.setString(1, username);
            userStmt.setString(2, password);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                int employeeId = userRs.getInt("employee_id");
                openMainFrame("user", employeeId);
                this.dispose();
                return;
            }


            JOptionPane.showMessageDialog(this, "用户名或密码错误！");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "登录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMainFrame(String role, int employeeId) {
        MainFrame mainFrame = new MainFrame(role, employeeId);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
