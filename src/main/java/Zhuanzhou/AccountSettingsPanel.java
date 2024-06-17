package Zhuanzhou;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AccountSettingsPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton updateButton;

    public AccountSettingsPanel(int employeeId) {
        setLayout(new GridLayout(3, 2));

        add(new JLabel("新用户名:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("新密码:"));
        passwordField = new JPasswordField();
        add(passwordField);

        updateButton = new JButton("更新");
        updateButton.addActionListener(e -> updateCredentials(employeeId));
        add(updateButton);
    }

    private void updateCredentials(int employeeId) {
        String newUsername = usernameField.getText();
        String newPassword = new String(passwordField.getPassword());

        String updateSql = "UPDATE user SET username = ?, password = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newPassword);
            pstmt.setInt(3, employeeId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "用户名和密码已更新！");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "更新失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
