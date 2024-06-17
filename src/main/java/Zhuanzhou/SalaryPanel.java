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
    private JTextField searchField;
    private JButton searchButton;

    public SalaryPanel() {
        setLayout(new BorderLayout());

        // 初始化表格和表格模型
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Employee ID", "Current Salary", "Adjust Date"});
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // 关闭自动调整列宽，以便更好地控制列宽度
        loadSalaries(); // 加载薪资数据到表格中

        // 添加表格到滚动面板中并添加到布局的中心
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 初始化搜索文本框和按钮
        searchField = new JTextField(10);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchSalaryByEmployeeId());

        // 创建搜索面板，并添加到布局的顶部
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        searchPanel.add(new JLabel("Employee ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);
    }

    private void loadSalaries() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, employee_id, salary, adjust_date FROM salary")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getInt("employee_id"));
                row.add(rs.getBigDecimal("salary"));
                row.add(rs.getDate("adjust_date"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchSalaryByEmployeeId() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Employee ID to search.");
            return;
        }

        try {
            int employeeId = Integer.parseInt(searchText);
            String sql = "SELECT id, employee_id, salary, adjust_date FROM salary WHERE employee_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, employeeId);

                ResultSet rs = pstmt.executeQuery();
                tableModel.setRowCount(0);
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getInt("employee_id"));
                    row.add(rs.getBigDecimal("salary"));
                    row.add(rs.getDate("adjust_date"));
                    tableModel.addRow(row);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Employee ID format. Please enter a valid integer.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
