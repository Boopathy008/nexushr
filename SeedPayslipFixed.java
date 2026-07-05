import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class SeedPayslipFixed {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            ResultSet rs = stmt.executeQuery("SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.username = 'Eswar'");
            if (!rs.next()) {
                System.out.println("Employee Eswar not found.");
                return;
            }
            String empIdStr = rs.getString("id");
            
            // Insert salary structure if not exists
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO salary_structures (employee_id, basic_salary, hra, transport_allowance, medical_allowance, other_allowances, tax_rate, pf_rate, effective_from) " +
                "VALUES (?::uuid, 50000, 20000, 5000, 5000, 0, 10, 12, '2026-01-01') " +
                "ON CONFLICT (employee_id) DO UPDATE SET basic_salary = 50000"
            );
            pstmt.setString(1, empIdStr);
            pstmt.executeUpdate();
            
            // Insert payroll run for July 2026
            PreparedStatement pstmt2 = conn.prepareStatement(
                "INSERT INTO payroll_runs (employee_id, pay_month, pay_year, working_days, basic_salary, hra, allowances, gross_salary, tax_deduction, pf_deduction, total_deductions, net_salary, status, processed_at) " +
                "VALUES (?::uuid, 7, 2026, 22, 50000, 20000, 10000, 80000, 4500, 8000, 12500, 67500, 'PAID', CURRENT_TIMESTAMP) " +
                "ON CONFLICT (employee_id, pay_month, pay_year) DO UPDATE SET status = 'PAID'"
            );
            pstmt2.setString(1, empIdStr);
            pstmt2.executeUpdate();
            System.out.println("Inserted salary structure and payroll run for Eswar for July 2026.");
        }
    }
}
