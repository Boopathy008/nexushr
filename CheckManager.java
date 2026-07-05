import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckManager {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT e.id, e.first_name, u.username, d.name as dept_name FROM users u LEFT JOIN employees e ON u.id = e.user_id LEFT JOIN departments d ON e.department_id = d.id WHERE u.username IN ('manager', 'admin')")) {
            while (rs.next()) {
                System.out.println(rs.getString("username") + " -> " + rs.getString("first_name") + ", Dept: " + rs.getString("dept_name"));
            }
        }
    }
}
