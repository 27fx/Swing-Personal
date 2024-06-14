package Zhuanzhou;// src/DepartmentPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DepartmentPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;
    private String role;

    public DepartmentPanel(String role) {
        this.role = role;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Code", "Manager", "Telephone"});
        table = new JTable(tableModel);
        loadDepartments();

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        if (role.equals("admin")) {
            addButton = new JButton("Add");
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);

            // Add action listeners
            addButton.addActionListener(e -> addDepartment());
            updateButton.addActionListener(e -> updateDepartment());
            deleteButton.addActionListener(e -> deleteDepartment());
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDepartments() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, code, manager, telephone FROM department")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("code"));
                row.add(rs.getString("manager"));
                row.add(rs.getString("telephone"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addDepartment() {
        JTextField nameField = new JTextField(10);
        JTextField codeField = new JTextField(10);
        JTextField managerField = new JTextField(10);
        JTextField telephoneField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(new JLabel("Code:"));
        myPanel.add(codeField);
        myPanel.add(new JLabel("Manager:"));
        myPanel.add(managerField);
        myPanel.add(new JLabel("Telephone:"));
        myPanel.add(telephoneField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter Department Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String code = codeField.getText();
            String manager = managerField.getText();
            String telephone = telephoneField.getText();

            String sql = "INSERT INTO department (name, code, manager, telephone) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, code);
                pstmt.setString(3, manager);
                pstmt.setString(4, telephone);
                pstmt.executeUpdate();
                loadDepartments();  // 重新加载部门信息
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDepartment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String code = (String) tableModel.getValueAt(selectedRow, 2);
            String manager = (String) tableModel.getValueAt(selectedRow, 3);
            String telephone = (String) tableModel.getValueAt(selectedRow, 4);

            JTextField nameField = new JTextField(name, 10);
            JTextField codeField = new JTextField(code, 10);
            JTextField managerField = new JTextField(manager, 10);
            JTextField telephoneField = new JTextField(telephone, 10);

            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Name:"));
            myPanel.add(nameField);
            myPanel.add(new JLabel("Code:"));
            myPanel.add(codeField);
            myPanel.add(new JLabel("Manager:"));
            myPanel.add(managerField);
            myPanel.add(new JLabel("Telephone:"));
            myPanel.add(telephoneField);

            int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Update Department Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                name = nameField.getText();
                code = codeField.getText();
                manager = managerField.getText();
                telephone = telephoneField.getText();

                String sql = "UPDATE department SET name = ?, code = ?, manager = ?, telephone = ? WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, code);
                    pstmt.setString(3, manager);
                    pstmt.setString(4, telephone);
                    pstmt.setInt(5, id);
                    pstmt.executeUpdate();
                    loadDepartments();  // 重新加载部门信息
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要更新的部门！");
        }
    }

    private void deleteDepartment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String sql = "DELETE FROM department WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadDepartments();  // 重新加载部门信息
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的部门！");
        }
    }
}