import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDepts {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            System.out.println("=== Departments ===");
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM departments");
            while (rs.next()) {
                System.out.println(rs.getString("name") + " -> " + rs.getString("id"));
            }
            
            System.out.println("\n=== Designations ===");
            ResultSet rs2 = stmt.executeQuery("SELECT d.id, d.title, dep.name as dept_name FROM designations d JOIN departments dep ON d.department_id = dep.id");
            while (rs2.next()) {
                System.out.println(rs2.getString("title") + " (" + rs2.getString("dept_name") + ") -> " + rs2.getString("id"));
            }
        }
    }
}
