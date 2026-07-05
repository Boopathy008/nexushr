import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckUser {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            System.out.println("=== Users ===");
            ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users WHERE username = 'Boopathy1'");
            while (rs.next()) {
                System.out.println(rs.getString("username") + " (Role: " + rs.getString("role") + ") -> user_id: " + rs.getString("id"));
            }
            
            System.out.println("\n=== Employees Linked to Boopathy1 ===");
            ResultSet rs2 = stmt.executeQuery("SELECT e.id, e.first_name, e.last_name FROM employees e JOIN users u ON e.user_id = u.id WHERE u.username = 'Boopathy1'");
            if (!rs2.next()) {
                 System.out.println("No employee record found for Boopathy1!");
            } else {
                 do {
                     System.out.println("Employee ID: " + rs2.getString("id") + " - " + rs2.getString("first_name") + " " + rs2.getString("last_name"));
                 } while(rs2.next());
            }
        }
    }
}
