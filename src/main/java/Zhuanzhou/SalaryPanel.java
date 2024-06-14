package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Vector;

public class SalaryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public SalaryPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Employee ID", "Current Salary", "Adjust Date"}); // Updated column headers
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Ensure auto resize mode is off for better control over column widths
        loadSalaries();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadSalaries() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, employee_id, salary, adjust_date FROM salary")) { // Include adjust_date in the query
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getInt("employee_id"));
                row.add(rs.getBigDecimal("salary"));
                row.add(rs.getDate("adjust_date")); // Get adjust_date from ResultSet
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
