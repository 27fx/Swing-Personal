package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.sql.Date;

public class EmployeePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, searchButton, clearSearchButton;
    private JTextField searchField;
    private String role;
    private int employeeId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public EmployeePanel(String role, int employeeId) {
        this.role = role;
        this.employeeId = employeeId;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        if(role.equals("admin")){
            tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Gender", "Age", "Marital Status", "Contact", "Education", "Title", "Salary", "Department ID", "Retire Status", "Resume", "Employee ID", "Salary Date"});
            table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            loadEmployees();
        } else {
            tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Gender", "Contact", "Title", "Department ID", "Retire Status", "Employee ID"});
            table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            loadEmployees2();
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        if (role.equals("admin")) {
            addButton = new JButton("Add");
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);

            // Add action listeners
            addButton.addActionListener(e -> addEmployee());
            updateButton.addActionListener(e -> updateEmployee());
            deleteButton.addActionListener(e -> deleteEmployee());
        } else {
            updateButton = new JButton("Update");
            buttonPanel.add(updateButton);
            updateButton.addActionListener(e -> updateOwnEmployee());
        }

        // Search components
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        clearSearchButton = new JButton("Clear Search");

        searchButton.addActionListener(e -> searchEmployee());
        clearSearchButton.addActionListener(e -> clearSearch());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        add(searchPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }



    private void updateOwnEmployee() {
        int selectedRow = 0;
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentGender = (String) tableModel.getValueAt(selectedRow, 2);
        String currentContact = (String) tableModel.getValueAt(selectedRow, 3);
        String currentTitle = (String) tableModel.getValueAt(selectedRow, 4);
        int currentDepartmentId = (int) tableModel.getValueAt(selectedRow, 5);
        int currentRetireStatus = (int) tableModel.getValueAt(selectedRow, 6);

        JTextField nameField = new JTextField(currentName, 10);
        nameField.setEditable(false); // make the field read-only
        JTextField genderField = new JTextField(currentGender, 10);
        genderField.setEditable(false); // make the field read-only
        JTextField contactField = new JTextField(currentContact, 10);
        JTextField titleField = new JTextField(currentTitle, 10);
        titleField.setEditable(false); // make the field read-only
        JTextField departmentIdField = new JTextField(String.valueOf(currentDepartmentId), 10);
        departmentIdField.setEditable(false); // make the field read-only
        JTextField retireStatusField = new JTextField(String.valueOf(currentRetireStatus), 10);
        retireStatusField.setEditable(false); // make the field read-only

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(6, 2, 5, 5));
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(new JLabel("Gender:"));
        myPanel.add(genderField);
        myPanel.add(new JLabel("Contact:"));
        myPanel.add(contactField);
        myPanel.add(new JLabel("Title:"));
        myPanel.add(titleField);
        myPanel.add(new JLabel("Department ID:"));
        myPanel.add(departmentIdField);
        myPanel.add(new JLabel("Retire Status:"));
        myPanel.add(retireStatusField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Update Your Contact Information", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                // Update employee contact
                String updateSql = "UPDATE employee SET contact=? WHERE id=?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, contactField.getText().trim());
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                loadEmployees2();
            } catch (SQLException e) {
                e.printStackTrace();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        }
    }


    private void loadEmployees() {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, gender, age, marry, contact, education, title, salary, department_id, retire_status, resume, employee_id, salary_date FROM employee")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getInt("age"));
                row.add(rs.getString("marry"));
                row.add(rs.getString("contact"));
                row.add(rs.getString("education"));
                row.add(rs.getString("title"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getInt("department_id"));
                row.add(rs.getInt("retire_status"));
                row.add(rs.getString("resume"));
                row.add(rs.getInt("employee_id"));
                row.add(rs.getDate("salary_date"));
                tableModel.addRow(row);
            }

            // 设置表格单元格不可编辑
            for (int col = 0; col < table.getColumnCount(); col++) {
                Class<?> columnClass = table.getColumnClass(col);
                table.setDefaultEditor(columnClass, null); // 设置为null以禁用编辑器
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchEmployee() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            loadEmployees();
            return;
        }

        String sql = "SELECT id, name, gender, age, marry, contact, education, title, salary, department_id, retire_status, resume, employee_id, salary_date " +
                "FROM employee WHERE " +
                "name LIKE ? OR " +
                "gender LIKE ? OR " +
                "CAST(age AS CHAR) LIKE ? OR " +
                "marry LIKE ? OR " +
                "contact LIKE ? OR " +
                "education LIKE ? OR " +
                "title LIKE ? OR " +
                "CAST(salary AS CHAR) LIKE ? OR " +
                "CAST(department_id AS CHAR) LIKE ? OR " +
                "CAST(retire_status AS CHAR) LIKE ? OR " +
                "resume LIKE ? OR " +
                "CAST(employee_id AS CHAR) LIKE ? OR " +
                "CAST(salary_date AS CHAR) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchQuery + "%";
            for (int i = 1; i <= 13; i++) {
                pstmt.setString(i, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0);

            DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setBackground(Color.WHITE); // Reset background color
                    if (value != null && value.toString().toLowerCase().contains(searchQuery.toLowerCase())) {
                        c.setBackground(Color.YELLOW); // Highlight if value contains search query
                    }
                    return c;
                }
            };

            for (int col = 0; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setCellRenderer(highlightRenderer);
            }

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getInt("age"));
                row.add(rs.getString("marry"));
                row.add(rs.getString("contact"));
                row.add(rs.getString("education"));
                row.add(rs.getString("title"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getInt("department_id"));
                row.add(rs.getInt("retire_status"));
                row.add(rs.getString("resume"));
                row.add(rs.getInt("employee_id"));
                row.add(rs.getDate("salary_date"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearSearch() {
        searchField.setText("");
        loadEmployees();

        // Reset cell renderers to default (remove highlighting)
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(table.getDefaultRenderer(Object.class));
        }
    }

    private void addEmployee() {
        JTextField nameField = new JTextField(10);
        JTextField genderField = new JTextField(10);
        JTextField ageField = new JTextField(10);
        JTextField marryField = new JTextField(10);
        JTextField contactField = new JTextField(10);
        JTextField educationField = new JTextField(10);
        JTextField titleField = new JTextField(10);
        JTextField salaryField = new JTextField(10);
        JTextField departmentIdField = new JTextField(10);
        JTextField retireStatusField = new JTextField(10);
        JTextArea resumeArea = new JTextArea(3, 10);
        JTextField employeeIdField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(12, 2, 5, 5));
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(new JLabel("Gender:"));
        myPanel.add(genderField);
        myPanel.add(new JLabel("Age:"));
        myPanel.add(ageField);
        myPanel.add(new JLabel("Marital Status:"));
        myPanel.add(marryField);
        myPanel.add(new JLabel("Contact:"));
        myPanel.add(contactField);
        myPanel.add(new JLabel("Education:"));
        myPanel.add(educationField);
        myPanel.add(new JLabel("Title:"));
        myPanel.add(titleField);
        myPanel.add(new JLabel("Salary:"));
        myPanel.add(salaryField);
        myPanel.add(new JLabel("Department ID:"));
        myPanel.add(departmentIdField);
        myPanel.add(new JLabel("Retire Status:"));
        myPanel.add(retireStatusField);
        myPanel.add(new JLabel("Resume:"));
        myPanel.add(new JScrollPane(resumeArea));
        myPanel.add(new JLabel("Employee ID:"));
        myPanel.add(employeeIdField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter Employee Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO employee (name, gender, age, marry, contact, education, title, salary, department_id, retire_status, resume, employee_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nameField.getText().trim());
                    pstmt.setString(2, genderField.getText().trim());
                    pstmt.setInt(3, Integer.parseInt(ageField.getText().trim()));
                    pstmt.setString(4, marryField.getText().trim());
                    pstmt.setString(5, contactField.getText().trim());
                    pstmt.setString(6, educationField.getText().trim());
                    pstmt.setString(7, titleField.getText().trim());
                    pstmt.setDouble(8, Double.parseDouble(salaryField.getText().trim()));
                    pstmt.setInt(9, Integer.parseInt(departmentIdField.getText().trim()));
                    pstmt.setInt(10, Integer.parseInt(retireStatusField.getText().trim()));
                    pstmt.setString(11, resumeArea.getText().trim());
                    pstmt.setInt(12, Integer.parseInt(employeeIdField.getText().trim()));
                    pstmt.executeUpdate();
                    loadEmployees();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentGender = (String) tableModel.getValueAt(selectedRow, 2);
        int currentAge = (int) tableModel.getValueAt(selectedRow, 3);
        String currentMarry = (String) tableModel.getValueAt(selectedRow, 4);
        String currentContact = (String) tableModel.getValueAt(selectedRow, 5);
        String currentEducation = (String) tableModel.getValueAt(selectedRow, 6);
        String currentTitle = (String) tableModel.getValueAt(selectedRow, 7);
        double currentSalary = (double) tableModel.getValueAt(selectedRow, 8);
        int currentDepartmentId = (int) tableModel.getValueAt(selectedRow, 9);
        int currentRetireStatus = (int) tableModel.getValueAt(selectedRow, 10);
        String currentResume = (String) tableModel.getValueAt(selectedRow, 11);
        int currentEmployeeId = (int) tableModel.getValueAt(selectedRow, 12);
        Date currentSalaryDate = (Date) tableModel.getValueAt(selectedRow, 13);

        JTextField nameField = new JTextField(currentName, 10);
        JTextField genderField = new JTextField(currentGender, 10);
        JTextField ageField = new JTextField(String.valueOf(currentAge), 10);
        JTextField marryField = new JTextField(currentMarry, 10);
        JTextField contactField = new JTextField(currentContact, 10);
        JTextField educationField = new JTextField(currentEducation, 10);
        JTextField titleField = new JTextField(currentTitle, 10);
        JTextField salaryField = new JTextField(String.valueOf(currentSalary), 10);
        JTextField departmentIdField = new JTextField(String.valueOf(currentDepartmentId), 10);
        JTextField retireStatusField = new JTextField(String.valueOf(currentRetireStatus), 10);
        JTextArea resumeArea = new JTextArea(currentResume, 3, 10);
        JTextField employeeIdField = new JTextField(String.valueOf(currentEmployeeId), 10);
        JTextField salaryDateField = new JTextField(dateFormat.format(currentSalaryDate), 10);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(13, 2, 5, 5));
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(new JLabel("Gender:"));
        myPanel.add(genderField);
        myPanel.add(new JLabel("Age:"));
        myPanel.add(ageField);
        myPanel.add(new JLabel("Marital Status:"));
        myPanel.add(marryField);
        myPanel.add(new JLabel("Contact:"));
        myPanel.add(contactField);
        myPanel.add(new JLabel("Education:"));
        myPanel.add(educationField);
        myPanel.add(new JLabel("Title:"));
        myPanel.add(titleField);
        myPanel.add(new JLabel("Salary:"));
        myPanel.add(salaryField);
        myPanel.add(new JLabel("Department ID:"));
        myPanel.add(departmentIdField);
        myPanel.add(new JLabel("Retire Status:"));
        myPanel.add(retireStatusField);
        myPanel.add(new JLabel("Resume:"));
        myPanel.add(new JScrollPane(resumeArea));
        myPanel.add(new JLabel("Employee ID:"));
        myPanel.add(employeeIdField);
        myPanel.add(new JLabel("Salary Date:"));
        myPanel.add(salaryDateField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Update Employee Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                // Update employee details
                String updateSql = "UPDATE employee SET name=?, gender=?, age=?, marry=?, contact=?, education=?, title=?, salary=?, department_id=?, retire_status=?, resume=?, employee_id=?, salary_date=? WHERE id=?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, nameField.getText().trim());
                    updateStmt.setString(2, genderField.getText().trim());
                    updateStmt.setInt(3, Integer.parseInt(ageField.getText().trim()));
                    updateStmt.setString(4, marryField.getText().trim());
                    updateStmt.setString(5, contactField.getText().trim());
                    updateStmt.setString(6, educationField.getText().trim());
                    updateStmt.setString(7, titleField.getText().trim());
                    updateStmt.setDouble(8, Double.parseDouble(salaryField.getText().trim()));
                    updateStmt.setInt(9, Integer.parseInt(departmentIdField.getText().trim()));
                    updateStmt.setInt(10, Integer.parseInt(retireStatusField.getText().trim()));
                    updateStmt.setString(11, resumeArea.getText().trim());
                    updateStmt.setInt(12, Integer.parseInt(employeeIdField.getText().trim()));
                    updateStmt.setDate(13, Date.valueOf(salaryDateField.getText().trim()));
                    updateStmt.setInt(14, id);
                    updateStmt.executeUpdate();
                }

                // Insert new salary record
                String insertSalarySql = "INSERT INTO salary (employee_id, salary, adjust_date) VALUES (?, ?, ?)";
                try (PreparedStatement insertSalaryStmt = conn.prepareStatement(insertSalarySql)) {
                    insertSalaryStmt.setInt(1, Integer.parseInt(employeeIdField.getText().trim()));
                    insertSalaryStmt.setBigDecimal(2, new BigDecimal(salaryField.getText().trim()));
                    insertSalaryStmt.setDate(3, Date.valueOf(salaryDateField.getText().trim()));
                    insertSalaryStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                loadEmployees();
            } catch (SQLException e) {
                e.printStackTrace();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        }
    }


    private void deleteEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employee WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadEmployees();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadEmployees2() {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, gender,  contact, title,  department_id, retire_status,  employee_id FROM employee")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));


                row.add(rs.getString("contact"));

                row.add(rs.getString("title"));

                row.add(rs.getInt("department_id"));
                row.add(rs.getInt("retire_status"));
                row.add(rs.getInt("employee_id"));

                tableModel.addRow(row);
            }

            // 设置表格单元格不可编辑
            for (int col = 0; col < table.getColumnCount(); col++) {
                Class<?> columnClass = table.getColumnClass(col);
                table.setDefaultEditor(columnClass, null); // 设置为null以禁用编辑器
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
