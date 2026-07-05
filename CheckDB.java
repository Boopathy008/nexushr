import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/nexushr";
        String user = "postgres";
        String password = "postgres123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, password_hash FROM users WHERE username IN ('manager', 'sanker', 'boopathy')")) {
            while (rs.next()) {
                System.out.println(rs.getString("username") + ": " + rs.getString("password_hash"));
            }
        }
    }
}
