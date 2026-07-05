import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Update Manager
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE users SET password_hash = ? WHERE username = 'manager'")) {
                pstmt.setString(1, "$2a$12$ExgQecEDkJ.OhSM3K03qj./ngRBoY7/0Z2/I/E8kQIpcmJh5niRUq");
                pstmt.executeUpdate();
                System.out.println("Manager password updated.");
            }
            
            // Update employees (sanker, boopathy)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE users SET password_hash = ? WHERE username = 'sanker'")) {
                pstmt.setString(1, "$2a$12$xaPFAaqX048vgs1H5uTuQ.5YaEII8ibqoN9Om.KLecURRJ0UZ1Uam");
                pstmt.executeUpdate();
                System.out.println("Sanker password updated.");
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE users SET password_hash = ? WHERE username = 'boopathy'")) {
                pstmt.setString(1, "$2a$12$MsqSvM/pVi1H40h8PZ0Ii.W4M7sL8ih7s2FeBVcSF7NRHwpDV6ede");
                pstmt.executeUpdate();
                System.out.println("Boopathy password updated.");
            }
        }
    }
}
