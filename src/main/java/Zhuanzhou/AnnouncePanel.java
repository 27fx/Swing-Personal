package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AnnouncePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;
    private String role;
    private int employeeId;

    public AnnouncePanel(String role, int employeeId) {
        this.role = role;
        this.employeeId = employeeId; // Set the employee ID
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Title", "Content", "Publish Time", "Creator ID"});
        table = new JTable(tableModel);
        loadAnnounces();

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
            addButton.addActionListener(e -> addAnnounce());
            updateButton.addActionListener(e -> updateAnnounce());
            deleteButton.addActionListener(e -> deleteAnnounce());
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAnnounces() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, content, publishtime, creatorId FROM Announcement")) {
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
                loadAnnounces();  // Reload announcements
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
                    loadAnnounces();  // Reload announcements
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
                loadAnnounces();  // Reload announcements
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an announcement to delete.");
        }
    }
}
