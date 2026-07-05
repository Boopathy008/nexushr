import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class SeedPayslip {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            // Find Eswar's employee record
            ResultSet rs = stmt.executeQuery("SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.username = 'Eswar'");
            if (!rs.next()) {
                System.out.println("Employee Eswar not found.");
                return;
            }
            String empIdStr = rs.getString("id");
            System.out.println("Found Eswar employee ID: " + empIdStr);
            
            // Insert salary structure if not exists
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO salary_structures (employee_id, basic_salary, hra, special_allowance, pf_deduction, tax_deduction) " +
                "VALUES (?::uuid, 50000, 20000, 10000, 4500, 8000) " +
                "ON CONFLICT (employee_id) DO UPDATE SET basic_salary = 50000"
            );
            pstmt.setString(1, empIdStr);
            pstmt.executeUpdate();
            
            // Insert payroll run for July 2026
            PreparedStatement pstmt2 = conn.prepareStatement(
                "INSERT INTO payroll_runs (employee_id, run_month, run_year, basic_salary, hra, special_allowance, gross_salary, pf_deduction, tax_deduction, total_deductions, net_salary, status, processed_date) " +
                "VALUES (?::uuid, 7, 2026, 50000, 20000, 10000, 80000, 4500, 8000, 12500, 67500, 'PAID', CURRENT_DATE) " +
                "ON CONFLICT (employee_id, run_month, run_year) DO UPDATE SET status = 'PAID'"
            );
            pstmt2.setString(1, empIdStr);
            pstmt2.executeUpdate();
            System.out.println("Inserted salary structure and payroll run for Eswar for July 2026.");
        }
    }
}
