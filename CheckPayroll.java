import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckPayroll {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            System.out.println("=== Employees ===");
            ResultSet rs = stmt.executeQuery("SELECT e.id, e.first_name, e.last_name, u.username FROM employees e JOIN users u ON e.user_id = u.id");
            while (rs.next()) {
                System.out.println(rs.getString("username") + " -> emp_id: " + rs.getString("id") + ", name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            }
            
            System.out.println("\n=== Payroll Runs ===");
            ResultSet rs2 = stmt.executeQuery("SELECT pr.id, e.first_name, pr.pay_month, pr.pay_year, pr.net_salary, pr.status FROM payroll_runs pr JOIN employees e ON pr.employee_id = e.id ORDER BY pr.pay_year DESC, pr.pay_month DESC LIMIT 10");
            while (rs2.next()) {
                System.out.println(rs2.getString("first_name") + " -> " + rs2.getString("pay_month") + "/" + rs2.getString("pay_year") + " net: " + rs2.getString("net_salary") + " status: " + rs2.getString("status"));
            }
            
            System.out.println("\n=== Payroll Runs (all) ===");
            ResultSet rs3 = stmt.executeQuery("SELECT count(*) as cnt FROM payroll_runs");
            if (rs3.next()) System.out.println("Total payroll runs: " + rs3.getString("cnt"));
            
            System.out.println("\n=== Salary Structures ===");
            ResultSet rs4 = stmt.executeQuery("SELECT count(*) as cnt FROM salary_structures");
            if (rs4.next()) System.out.println("Total salary structures: " + rs4.getString("cnt"));
        }
    }
}
