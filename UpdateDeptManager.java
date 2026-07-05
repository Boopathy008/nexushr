import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateDeptManager {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE departments SET manager_id = (SELECT id FROM users WHERE username = 'manager') WHERE name = 'Engineering'")) {
                int rows = pstmt.executeUpdate();
                System.out.println("Engineering department updated: " + rows + " rows.");
            }
        }
    }
}
