package Zhuanzhou;// src/AttendancePanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AttendancePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;
    private String role;

    public AttendancePanel(String role) {
        this.role = role;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Employee ID", "Date", "Check-in Time", "Check-out Time", "Status"});
        table = new JTable(tableModel);
        loadAttendance();

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
            addButton.addActionListener(e -> addAttendance());
            updateButton.addActionListener(e -> updateAttendance());
            deleteButton.addActionListener(e -> deleteAttendance());
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAttendance() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, employeeid, date, checkintime, checkouttime, status FROM Attendance")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getInt("employeeid"));
                row.add(rs.getDate("date"));
                row.add(rs.getTime("checkintime"));
                row.add(rs.getTime("checkouttime"));
                row.add(rs.getString("status"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addAttendance() {
        JTextField employeeIdField = new JTextField(10);
        JTextField dateField = new JTextField(10);
        JTextField checkInTimeField = new JTextField(10);
        JTextField checkOutTimeField = new JTextField(10);
        JTextField statusField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Employee ID:"));
        myPanel.add(employeeIdField);
        myPanel.add(new JLabel("Date:"));
        myPanel.add(dateField);
        myPanel.add(new JLabel("Check-in Time:"));
        myPanel.add(checkInTimeField);
        myPanel.add(new JLabel("Check-out Time:"));
        myPanel.add(checkOutTimeField);
        myPanel.add(new JLabel("Status:"));
        myPanel.add(statusField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter Attendance Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int employeeId = Integer.parseInt(employeeIdField.getText());
            Date date = Date.valueOf(dateField.getText());
            Time checkInTime = Time.valueOf(checkInTimeField.getText());
            Time checkOutTime = Time.valueOf(checkOutTimeField.getText());
            String status = statusField.getText();

            String sql = "INSERT INTO Attendance (employeeid, date, checkintime, checkouttime, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, employeeId);
                pstmt.setDate(2, date);
                pstmt.setTime(3, checkInTime);
                pstmt.setTime(4, checkOutTime);
                pstmt.setString(5, status);
                pstmt.executeUpdate();
                loadAttendance();  // 重新加载考勤信息
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAttendance() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int employeeId = (int) tableModel.getValueAt(selectedRow, 1);
            Date date = (Date) tableModel.getValueAt(selectedRow, 2);
            Time checkInTime = (Time) tableModel.getValueAt(selectedRow, 3);
            Time checkOutTime = (Time) tableModel.getValueAt(selectedRow, 4);
            String status = (String) tableModel.getValueAt(selectedRow, 5);

            JTextField employeeIdField = new JTextField(String.valueOf(employeeId), 10);
            JTextField dateField = new JTextField(String.valueOf(date), 10);
            JTextField checkInTimeField = new JTextField(String.valueOf(checkInTime), 10);
            JTextField checkOutTimeField = new JTextField(String.valueOf(checkOutTime), 10);
            JTextField statusField = new JTextField(status, 10);

            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Employee ID:"));
            myPanel.add(employeeIdField);
            myPanel.add(new JLabel("Date:"));
            myPanel.add(dateField);
            myPanel.add(new JLabel("Check-in Time:"));
            myPanel.add(checkInTimeField);
            myPanel.add(new JLabel("Check-out Time:"));
            myPanel.add(checkOutTimeField);
            myPanel.add(new JLabel("Status:"));
            myPanel.add(statusField);

            int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Update Attendance Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                employeeId = Integer.parseInt(employeeIdField.getText());
                date = Date.valueOf(dateField.getText());
                checkInTime = Time.valueOf(checkInTimeField.getText());
                checkOutTime = Time.valueOf(checkOutTimeField.getText());
                status = statusField.getText();

                String sql = "UPDATE Attendance SET employeeid = ?, date = ?, checkintime = ?, checkouttime = ?, status = ? WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, employeeId);
                    pstmt.setDate(2, date);
                    pstmt.setTime(3, checkInTime);
                    pstmt.setTime(4, checkOutTime);
                    pstmt.setString(5, status);
                    pstmt.setInt(6, id);
                    pstmt.executeUpdate();
                    loadAttendance();  // 重新加载考勤信息
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要更新的考勤记录！");
        }
    }

    private void deleteAttendance() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String sql = "DELETE FROM Attendance WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadAttendance();  // 重新加载考勤信息
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的考勤记录！");
        }
    }
}
