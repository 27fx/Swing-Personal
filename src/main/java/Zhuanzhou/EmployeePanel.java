package Zhuanzhou;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class EmployeePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, searchButton, clearSearchButton;
    private JTextField searchField;
    private String role;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public EmployeePanel(String role) {
        this.role = role;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Gender", "Age", "Marital Status", "Contact", "Education", "Title", "Salary", "Department ID", "Retire Status", "Resume", "Employee ID", "Salary Date"});
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        loadEmployees();

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

            // Custom renderer for highlighting matched cells
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

            // Apply the custom renderer to all columns
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
            String name = nameField.getText();
            String gender = genderField.getText();
            int age = Integer.parseInt(ageField.getText());
            String marry = marryField.getText();
            String contact = contactField.getText();
            String education = educationField.getText();
            String title = titleField.getText();
            double salary = Double.parseDouble(salaryField.getText());
            int departmentId = Integer.parseInt(departmentIdField.getText());
            int retireStatus = Integer.parseInt(retireStatusField.getText());
            String resume = resumeArea.getText();
            int employeeId = Integer.parseInt(employeeIdField.getText());

            String sql = "INSERT INTO employee (name, gender, age, marry, contact, education, title, salary, department_id, retire_status, resume, employee_id, salary_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, gender);
                pstmt.setInt(3, age);
                pstmt.setString(4, marry);
                pstmt.setString(5, contact);
                pstmt.setString(6, education);
                pstmt.setString(7, title);
                pstmt.setDouble(8, salary);
                pstmt.setInt(9, departmentId);
                pstmt.setInt(10, retireStatus);
                pstmt.setString(11, resume);
                pstmt.setInt(12, employeeId);
                pstmt.setDate(13, getNextMonthDate());
                pstmt.executeUpdate();

                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newEmployeeId = generatedKeys.getInt(1);
                    updateEmployeeSalary(newEmployeeId, BigDecimal.valueOf(salary));
                }

                loadEmployees();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String gender = (String) tableModel.getValueAt(selectedRow, 2);
            int age = (int) tableModel.getValueAt(selectedRow, 3);
            String marry = (String) tableModel.getValueAt(selectedRow, 4);
            String contact = (String) tableModel.getValueAt(selectedRow, 5);
            String education = (String) tableModel.getValueAt(selectedRow, 6);
            String title = (String) tableModel.getValueAt(selectedRow, 7);
            double salary = (double) tableModel.getValueAt(selectedRow, 8);
            int departmentId = (int) tableModel.getValueAt(selectedRow, 9);
            int retireStatus = (int) tableModel.getValueAt(selectedRow, 10);
            String resume = (String) tableModel.getValueAt(selectedRow, 11);
            int employeeId = (int) tableModel.getValueAt(selectedRow, 12);
            Date salaryDate = (Date) tableModel.getValueAt(selectedRow, 13);

            JTextField nameField = new JTextField(name, 10);
            JTextField genderField = new JTextField(gender, 10);
            JTextField ageField = new JTextField(String.valueOf(age), 10);
            JTextField marryField = new JTextField(marry, 10);
            JTextField contactField = new JTextField(contact, 10);
            JTextField educationField = new JTextField(education, 10);
            JTextField titleField = new JTextField(title, 10);
            JTextField salaryField = new JTextField(String.valueOf(salary), 10);
            JTextField departmentIdField = new JTextField(String.valueOf(departmentId), 10);
            JTextField retireStatusField = new JTextField(String.valueOf(retireStatus), 10);
            JTextArea resumeArea = new JTextArea(resume, 3, 10);
            JTextField employeeIdField = new JTextField(String.valueOf(employeeId), 10);
            JTextField salaryDateField = new JTextField(dateFormat.format(salaryDate), 10);

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
                name = nameField.getText();
                gender = genderField.getText();
                age = Integer.parseInt(ageField.getText());
                marry = marryField.getText();
                contact = contactField.getText();
                education = educationField.getText();
                title = titleField.getText();
                salary = Double.parseDouble(salaryField.getText());
                departmentId = Integer.parseInt(departmentIdField.getText());
                retireStatus = Integer.parseInt(retireStatusField.getText());
                resume = resumeArea.getText();
                employeeId = Integer.parseInt(employeeIdField.getText());
                Date newSalaryDate = Date.valueOf(salaryDateField.getText());

                String sql = "UPDATE employee SET name=?, gender=?, age=?, marry=?, contact=?, education=?, title=?, salary=?, department_id=?, retire_status=?, resume=?, employee_id=?, salary_date=? WHERE id=?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, gender);
                    pstmt.setInt(3, age);
                    pstmt.setString(4, marry);
                    pstmt.setString(5, contact);
                    pstmt.setString(6, education);
                    pstmt.setString(7, title);
                    pstmt.setDouble(8, salary);
                    pstmt.setInt(9, departmentId);
                    pstmt.setInt(10, retireStatus);
                    pstmt.setString(11, resume);
                    pstmt.setInt(12, employeeId);
                    pstmt.setDate(13, newSalaryDate);
                    pstmt.setInt(14, id);
                    pstmt.executeUpdate();

                    updateEmployeeSalary(employeeId, BigDecimal.valueOf(salary));
                    loadEmployees();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this employee?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM employee WHERE id=?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    loadEmployees();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployeeSalary(int employeeId, BigDecimal newSalary) {
        String sql = "INSERT INTO salary (employee_id, salary, adjust_date) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE salary=?, adjust_date=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setBigDecimal(2, newSalary);
            pstmt.setDate(3, new Date(System.currentTimeMillis()));
            pstmt.setBigDecimal(4, newSalary);
            pstmt.setDate(5, new Date(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Date getNextMonthDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        return new Date(calendar.getTimeInMillis());
    }

    public static void main(String[] args) {
        // Example of using EmployeePanel in a JFrame
        JFrame frame = new JFrame("Employee Management System");
        EmployeePanel employeePanel = new EmployeePanel("admin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(employeePanel);
        frame.pack();
        frame.setVisible(true);
    }
}

