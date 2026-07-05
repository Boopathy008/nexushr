import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SeedPayroll {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
        
            // First, get the employee IDs for manager and john.doe
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT e.id, u.username FROM employees e JOIN users u ON e.user_id = u.id WHERE u.username IN ('manager', 'john.doe')");
            
            while (rs.next()) {
                String empId = rs.getString("id");
                String username = rs.getString("username");
                System.out.println("Processing: " + username + " (" + empId + ")");
                
                // Insert salary structure if not exists
                PreparedStatement checkSal = conn.prepareStatement("SELECT COUNT(*) FROM salary_structures WHERE employee_id = ?::uuid");
                checkSal.setString(1, empId);
                ResultSet checkRs = checkSal.executeQuery();
                checkRs.next();
                if (checkRs.getInt(1) == 0) {
                    PreparedStatement salStmt = conn.prepareStatement(
                        "INSERT INTO salary_structures (employee_id, basic_salary, hra, transport_allowance, medical_allowance, other_allowances, tax_rate, pf_rate, effective_from) " +
                        "VALUES (?::uuid, 50000, 20000, 2000, 1500, 1000, 10, 12, '2024-01-01')");
                    salStmt.setString(1, empId);
                    salStmt.executeUpdate();
                    System.out.println("  Created salary structure for " + username);
                } else {
                    System.out.println("  Salary structure already exists for " + username);
                }
                
                // Insert payroll run for July 2026 if not exists
                PreparedStatement checkPay = conn.prepareStatement("SELECT COUNT(*) FROM payroll_runs WHERE employee_id = ?::uuid AND pay_month = 7 AND pay_year = 2026");
                checkPay.setString(1, empId);
                ResultSet payRs = checkPay.executeQuery();
                payRs.next();
                if (payRs.getInt(1) == 0) {
                    PreparedStatement payStmt = conn.prepareStatement(
                        "INSERT INTO payroll_runs (employee_id, pay_month, pay_year, working_days, present_days, leave_days, lop_days, basic_salary, hra, allowances, gross_salary, tax_deduction, pf_deduction, lop_deduction, other_deductions, total_deductions, net_salary, status, processed_at) " +
                        "VALUES (?::uuid, 7, 2026, 23, 22, 1, 0, 50000, 20000, 4500, 74500, 7450, 6000, 0, 0, 13450, 61050, 'PROCESSED', NOW())");
                    payStmt.setString(1, empId);
                    payStmt.executeUpdate();
                    System.out.println("  Created July 2026 payroll for " + username);
                } else {
                    System.out.println("  Payroll already exists for " + username);
                }
                
                // Also insert June 2026 payroll
                PreparedStatement checkJune = conn.prepareStatement("SELECT COUNT(*) FROM payroll_runs WHERE employee_id = ?::uuid AND pay_month = 6 AND pay_year = 2026");
                checkJune.setString(1, empId);
                ResultSet juneRs = checkJune.executeQuery();
                juneRs.next();
                if (juneRs.getInt(1) == 0) {
                    PreparedStatement juneStmt = conn.prepareStatement(
                        "INSERT INTO payroll_runs (employee_id, pay_month, pay_year, working_days, present_days, leave_days, lop_days, basic_salary, hra, allowances, gross_salary, tax_deduction, pf_deduction, lop_deduction, other_deductions, total_deductions, net_salary, status, processed_at) " +
                        "VALUES (?::uuid, 6, 2026, 22, 21, 1, 0, 50000, 20000, 4500, 74500, 7450, 6000, 0, 0, 13450, 61050, 'PROCESSED', NOW())");
                    juneStmt.setString(1, empId);
                    juneStmt.executeUpdate();
                    System.out.println("  Created June 2026 payroll for " + username);
                }
            }
            System.out.println("Done!");
        }
    }
}
