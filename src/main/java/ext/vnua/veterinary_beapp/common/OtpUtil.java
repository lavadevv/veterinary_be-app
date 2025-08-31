package ext.vnua.veterinary_beapp.common;

import java.util.Random;

public class OtpUtil {
    public static String generateOtp() {
        Random random = new Random();
        int radomNumber = random.nextInt(999999);
        String output = Integer.toString(radomNumber);
        while (output.length() < 6) {
            output = 0 + output;
        }
        return output;
    }
}
