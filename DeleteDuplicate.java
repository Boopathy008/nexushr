import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DeleteDuplicate {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Delete the employee directly
            PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM employees WHERE employee_code = 'ENG260003'"
            );
            int updated = pstmt.executeUpdate();
            
            // Delete the associated user
            PreparedStatement pstmt2 = conn.prepareStatement(
                "DELETE FROM users WHERE id = '8551c1cd-ef48-49b3-b33b-c77d39e67f5e'::uuid"
            );
            pstmt2.executeUpdate();
            
            System.out.println("Deleted duplicate employee (ENG260003).");
        }
    }
}
