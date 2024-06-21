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

        public AttendancePanel(String role,int employeeId) {
            this.employeeId = employeeId;
            setLayout(new BorderLayout());

            // Create buttons for check-in and check-out
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
            JButton checkInButton = new JButton("签到");
            checkInButton.addActionListener(e -> checkIn());
            JButton checkOutButton = new JButton("签退");
            checkOutButton.addActionListener(e -> checkOut());
            buttonPanel.add(checkInButton);
            buttonPanel.add(checkOutButton);
            add(buttonPanel, BorderLayout.NORTH);

            // Create a table to display attendance records
            attendanceTable = new JTable();
            add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

            if (role.equals("admin")){

                loadAttendanceRecords2();

            }

            else {loadAttendanceRecords();}
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

        private void loadAttendanceRecords() {
            String sql = "SELECT * FROM Attendance WHERE EmployeeID = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, employeeId);
                ResultSet rs = stmt.executeQuery();

                // Assuming you have a method to convert ResultSet to TableModel
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

        private void loadAttendanceRecords2() {
            String sql = "SELECT * FROM Attendance";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                 ResultSet rs = stmt.executeQuery();

                // Assuming you have a method to convert ResultSet to TableModel
                attendanceTable.setModel(buildTableModel(rs));
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "加载考勤记录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
