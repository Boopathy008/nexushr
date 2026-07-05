import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckEmployees {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            System.out.println("=== Sanker Ganesh Employees ===");
            ResultSet rs = stmt.executeQuery("SELECT id, employee_code, first_name, last_name, user_id, status FROM employees WHERE first_name ILIKE '%sanker%'");
            while (rs.next()) {
                System.out.println(rs.getString("first_name") + " " + rs.getString("last_name") + " - Code: " + rs.getString("employee_code") + " - Status: " + rs.getString("status") + " - ID: " + rs.getString("id") + " - User ID: " + rs.getString("user_id"));
            }
        }
    }
}
