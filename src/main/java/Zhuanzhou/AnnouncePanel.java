package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AnnouncePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, searchButton, clearSearchButton;
    private JTextField searchField;
    private String role;
    private int employeeId;

    public AnnouncePanel(String role, int employeeId) {
        this.role = role;
        this.employeeId = employeeId; // Set the employee ID
        setLayout(new BorderLayout());

        // 初始化搜索组件
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        clearSearchButton = new JButton("Clear Search");

        searchButton.addActionListener(e -> searchAnnounces());
        clearSearchButton.addActionListener(e -> clearSearch());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        add(searchPanel, BorderLayout.NORTH);

        // 初始化表格和按钮面板
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Title", "Content", "Publish Time", "Creator ID"});
        table = new JTable(tableModel);

        // 使用自定义渲染器来高亮显示搜索关键词
        table.setDefaultRenderer(Object.class, new HighlightRenderer());

        loadAnnounces();  // 初始加载所有公告

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        if (role.equals("admin")) {
            addButton = new JButton("Add");
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);

            // 添加按钮的监听器
            addButton.addActionListener(e -> addAnnounce());
            updateButton.addActionListener(e -> updateAnnounce());
            deleteButton.addActionListener(e -> deleteAnnounce());
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAnnounces() {
        String sql = "SELECT id, title, content, publishtime, creatorId FROM Announcement";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("title"));
                row.add(rs.getString("content"));
                row.add(rs.getTimestamp("publishtime"));
                row.add(rs.getInt("creatorId"));
                tableModel.addRow(row);
            }

            // 禁用表格的编辑功能
            for (int col = 0; col < table.getColumnCount(); col++) {
                Class<?> columnClass = table.getColumnClass(col);
                table.setDefaultEditor(columnClass, null); // 设置为null以禁用编辑器
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addAnnounce() {
        JTextField titleField = new JTextField(10);
        JTextField contentField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Title:"));
        myPanel.add(titleField);
        myPanel.add(new JLabel("Content:"));
        myPanel.add(contentField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter Announcement Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String content = contentField.getText();

            String sql = "INSERT INTO Announcement (title, content, publishtime, creatorId) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, content);
                pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                pstmt.setInt(4, employeeId); // Use the employee ID
                pstmt.executeUpdate();
                loadAnnounces();  // 重新加载公告
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAnnounce() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            String content = (String) tableModel.getValueAt(selectedRow, 2);
            Timestamp signInTime = (Timestamp) tableModel.getValueAt(selectedRow, 3);
            Timestamp signOutTime = new Timestamp(System.currentTimeMillis());

            // Check if the sign-out time is valid
            if (!isValidTime(signInTime, signOutTime)) {
                JOptionPane.showMessageDialog(this, "Sign-out time must be after sign-in time.");
                return; // Exit method without updating
            }

            JTextField titleField = new JTextField(title, 10);
            JTextField contentField = new JTextField(content, 10);

            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Title:"));
            myPanel.add(titleField);
            myPanel.add(new JLabel("Content:"));
            myPanel.add(contentField);

            int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Update Announcement Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                title = titleField.getText();
                content = contentField.getText();

                String sql = "UPDATE Announcement SET title = ?, content = ? WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, title);
                    pstmt.setString(2, content);
                    pstmt.setInt(3, id);
                    pstmt.executeUpdate();
                    loadAnnounces();  // 重新加载公告
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an announcement to update.");
        }
    }

    private void deleteAnnounce() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String sql = "DELETE FROM Announcement WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadAnnounces();  // 重新加载公告
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an announcement to delete.");
        }
    }

    private void searchAnnounces() {
        String searchText = searchField.getText().trim();
        String sql = "SELECT id, title, content, publishtime, creatorId FROM Announcement WHERE title LIKE ? OR content LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("title"));
                row.add(rs.getString("content"));
                row.add(rs.getTimestamp("publishtime"));
                row.add(rs.getInt("creatorId"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearSearch() {
        searchField.setText("");
        loadAnnounces();  // 清除搜索并重新加载所有公告
    }

    // 自定义渲染器，用于将搜索关键词高亮显示
    private class HighlightRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 如果是字符串类型，并且搜索字段非空，且当前单元格的文本包含搜索文本，则进行高亮处理
            if (value instanceof String && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().trim();
                String text = (String) value;
                String htmlText = text.replaceAll("(?i)" + searchText, "<span style='background: yellow;'>$0</span>");
                ((JLabel) cellComponent).setText("<html>" + htmlText + "</html>");
            }

            return cellComponent;
        }
    }

    private boolean isValidTime(Timestamp signInTime, Timestamp signOutTime) {
        // Check if sign-out time is after sign-in time
        if (signOutTime != null && signOutTime.before(signInTime)) {
            return false;
        }
        return true;
    }
}
