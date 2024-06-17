package Zhuanzhou;

import javax.swing.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private String role;
    private int employeeId;

    public MainFrame(String role, int employeeId) {
        this.role = role;
        this.employeeId = employeeId;
        setTitle("人事管理系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.add("员工管理", new EmployeePanel(role));
        tabbedPane.add("部门管理", new DepartmentPanel(role));
        tabbedPane.add("考勤管理", new AttendancePanel(role));
        tabbedPane.add("公告管理", new AnnouncePanel(role, employeeId));
        tabbedPane.add("工资管理", new SalaryPanel());

        add(tabbedPane);

        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Assuming you have a LoginFrame that handles authentication
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
