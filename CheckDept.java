import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDept {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT d.id, d.name, d.manager_id, u.username FROM departments d LEFT JOIN users u ON d.manager_id = u.id")) {
            while (rs.next()) {
                System.out.println(rs.getString("name") + " -> manager_id: " + rs.getString("manager_id") + " (User: " + rs.getString("username") + ")");
            }
            
            ResultSet rs2 = stmt.executeQuery("SELECT id, username FROM users WHERE username = 'manager'");
            if (rs2.next()) {
                System.out.println("Manager User ID: " + rs2.getString("id"));
            }
        }
    }
}
