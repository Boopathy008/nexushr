import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateEmployee {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Find Boopathy1
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE username = 'Boopathy1'");
            if (!rs.next()) {
                System.out.println("User Boopathy1 not found");
                return;
            }
            String userId = rs.getString("id");
            
            // Create employee
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO employees (user_id, department_id, designation_id, employee_code, first_name, last_name, date_of_joining, status) " +
                "VALUES (?::uuid, '67f159b5-c3b4-4309-b4f6-f5ed43d09e1d'::uuid, 'fb6c2829-a266-4823-9264-6b48a08f1c50'::uuid, 'ENG260005', 'Boopathy', 'One', CURRENT_DATE, 'ACTIVE') " +
                "ON CONFLICT (user_id) DO NOTHING"
            );
            pstmt.setString(1, userId);
            int updated = pstmt.executeUpdate();
            System.out.println("Inserted employee record: " + updated);
            
            // Set as manager of Engineering
            PreparedStatement pstmt2 = conn.prepareStatement(
                "UPDATE departments SET manager_id = ?::uuid WHERE id = '67f159b5-c3b4-4309-b4f6-f5ed43d09e1d'::uuid"
            );
            pstmt2.setString(1, userId);
            pstmt2.executeUpdate();
            System.out.println("Set Boopathy1 as manager of Engineering.");
        }
    }
}
