package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Vector;

public class AttendancePanel extends JPanel {
    private int employeeId;
    private JTable attendanceTable;

    public AttendancePanel(String role, int employeeId) {
        this.employeeId = employeeId;
        setLayout(new BorderLayout());

        if(role.equals("user")) {

            // Create buttons for check-in, check-out, and set leave
            JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
            JButton checkInButton = new JButton("签到");
            checkInButton.addActionListener(e -> checkIn());
            JButton checkOutButton = new JButton("签退");
            checkOutButton.addActionListener(e -> checkOut());
            JButton setLeaveButton = new JButton("请假");
            setLeaveButton.addActionListener(e -> setLeave());
            buttonPanel.add(checkInButton);
            buttonPanel.add(checkOutButton);
            buttonPanel.add(setLeaveButton);
            add(buttonPanel, BorderLayout.NORTH);
        }
        // Create a table to display attendance records
        attendanceTable = new JTable();
        add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        // Load attendance records based on role
        if ("admin".equals(role)) {
            loadAllAttendanceRecords();
        } else {
            loadAttendanceRecords();
        }
    }

    private void checkIn() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        String status = "Present";
        String checkInStatus = "Completed";
        if (now.isAfter(LocalTime.of(9, 0))) {
            status = "Late";
        }

        String sql = "INSERT INTO Attendance (EmployeeID, Date, CheckInTime, Status, CheckInStatus) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE CheckInTime = VALUES(CheckInTime), Status = VALUES(Status), CheckInStatus = VALUES(CheckInStatus)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(today));
            stmt.setTime(3, Time.valueOf(now));
            stmt.setString(4, status);
            stmt.setString(5, checkInStatus);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "签到成功！");
            loadAttendanceRecords();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "签到失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkOut() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        String status = "Present";
        String checkOutStatus = "Completed";
        if (now.isBefore(LocalTime.of(18, 0))) {
            status = "Early Leave";
        }

        String sql = "UPDATE Attendance SET CheckOutTime = ?, Status = ?, CheckOutStatus = ? WHERE EmployeeID = ? AND Date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTime(1, Time.valueOf(now));
            stmt.setString(2, status);
            stmt.setString(3, checkOutStatus);
            stmt.setInt(4, employeeId);
            stmt.setDate(5, Date.valueOf(today));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "签退成功！");
            loadAttendanceRecords();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "签退失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setLeave() {
        LocalDate today = LocalDate.now();

        String status = "Leave";
        String checkInStatus = "Completed";
        String checkOutStatus = "Completed";

        String sql = "INSERT INTO Attendance (EmployeeID, Date, Status, CheckInStatus, CheckOutStatus) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE Status = VALUES(Status), CheckInStatus = VALUES(CheckInStatus), CheckOutStatus = VALUES(CheckOutStatus)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(today));
            stmt.setString(3, status);
            stmt.setString(4, checkInStatus);
            stmt.setString(5, checkOutStatus);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "已设定为请假！");
            loadAttendanceRecords();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "设定请假失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceRecords() {
        String sql = "SELECT * FROM Attendance WHERE EmployeeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            attendanceTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载考勤记录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllAttendanceRecords() {
        String sql = "SELECT * FROM Attendance";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            attendanceTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载考勤记录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
}
