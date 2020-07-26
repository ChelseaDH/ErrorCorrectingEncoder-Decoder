package correcter;

import java.util.Random;

public class Errors {

    public static void addRandom(byte[] bytes) {
        Random r = new Random();
        // Iterate through the input
        // Complete a bit shift based on a random int
        // Add the resulting byte to the file
        for (int i = 0; i < bytes.length; i++) {
            int shift = 1 << r.nextInt(8);
            bytes[i] ^= shift;
        }
    }
}
