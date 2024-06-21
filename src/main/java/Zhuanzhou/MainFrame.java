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

        // Add tabs based on user role
        tabbedPane.add("员工模块", new EmployeePanel(role, employeeId));
        tabbedPane.add("部门模块", new DepartmentPanel(role));
        tabbedPane.add("考勤模块", new AttendancePanel(role,employeeId));
        tabbedPane.add("公告模块", new AnnouncePanel(role, employeeId));

        if ("admin".equals(role)) {
            tabbedPane.add("工资记录模块", new SalaryPanel(role));
        }

        add(tabbedPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu("选项");
        JMenuItem accountSettingsItem = new JMenuItem("账户设置");
        accountSettingsItem.addActionListener(e -> openAccountSettings());
        optionsMenu.add(accountSettingsItem);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);

        setLocationRelativeTo(null);
    }

    private void openAccountSettings() {
        JFrame accountSettingsFrame = new JFrame("账户设置");
        AccountSettingsPanel accountSettingsPanel = new AccountSettingsPanel(employeeId);
        accountSettingsFrame.add(accountSettingsPanel);
        accountSettingsFrame.pack();
        accountSettingsFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Assuming you have a LoginFrame that handles authentication
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
