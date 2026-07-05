import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        System.out.println("MANAGER: " + encoder.encode("Manager@1234"));
        System.out.println("SANKER: " + encoder.encode("sanker123"));
        System.out.println("BOOPATHY: " + encoder.encode("boopathy123"));
    }
}
