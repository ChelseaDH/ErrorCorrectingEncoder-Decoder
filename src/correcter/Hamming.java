package correcter;

public class Hamming {

    public static byte[] encode(byte[] bytes) {
        // Implement Hamming code [7,4]
        int numberOfBits = bytes.length * 8;
        byte[] encoded = new byte[bytes.length * 2];

        int[] currentBits;

        int byteNo, bitNo;

        for (int bitCounter = 0; bitCounter < numberOfBits; bitCounter += 4) {
            currentBits = new int[]{0, 0, 0, 0};
            for (int i = 0; i < 4; i++) {
                byteNo = (bitCounter + i) / 8;
                bitNo = (bitCounter + i) % 8;

                if (byteNo < bytes.length) {
                    currentBits[i] = (bytes[byteNo] >> (7 - bitNo)) & 1;
                }
            }

            // Calculate parities
            // parity = 1 => odd number of 1s in input
            // parity = 0 => even number of 1s in input
            int parity1 = Parity.calculate(currentBits[0], currentBits[1], currentBits[3]);
            int parity2 = Parity.calculate(currentBits[0], currentBits[2], currentBits[3]);
            int parity3 = Parity.calculate(currentBits[1], currentBits[2], currentBits[3]);

            int newByte = (parity1 << 7 | parity2 << 6 | currentBits[0] << 5 | parity3 << 4
                    | currentBits[1] << 3 | currentBits[2] << 2 | currentBits[3] << 1);

            encoded[bitCounter / 4] = (byte) newByte;
        }
        return encoded;
    }

    public static byte[] correct(byte[] bytes) {
        // Correct the error in each byte
        byte[] corrected = new byte[bytes.length];

        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {

            byte[] bits = new byte[8];

            // Obtain the individual bits
            for (int j = 0; j < 8; j++) {
                bits[j] = (byte) ((bytes[byteIndex] >> (7 - j)) & 1);
            }

            // Check if parity bits are correct
            boolean[] isParityCorrect = new boolean[3];
            isParityCorrect[0] = Parity.check(bits[2], bits[4], bits[6], bits[0]);
            isParityCorrect[1] = Parity.check(bits[2], bits[5], bits[6], bits[1]);
            isParityCorrect[2] = Parity.check(bits[4], bits[5], bits[6], bits[3]);

            // Find number of errors
            int noErrors = countFalse(isParityCorrect);

            // Find index of the error and correct it
            int errorIndex = findErrorIndex(isParityCorrect, noErrors);
            bits[errorIndex] ^= 1;

            // Put the byte back together in corrected
            int correctedByte = 0;
            for (int j = 0; j < 8; j++) {
                correctedByte = correctedByte | (bits[j] << (7 - j));
            }

            corrected[byteIndex] = (byte) correctedByte;
        }
        return corrected;
    }

    private static int countFalse(boolean[] bools) {
        int noFalse = 0;

        for (boolean b: bools) {
            if (!b) {
                noFalse++;
            }
        }

        return noFalse;
    }

    private static int findErrorIndex(boolean[] bools, int noErrors) {
        switch (noErrors) {
            case 0:
                return 7;
            case 1:
                if (!bools[0]) {return 0;}
                if (!bools[1]) {return 1;}
                if (!bools[2]) {return 3;}
            case 2:
                if (bools[0] == bools[1]) {return 2;}
                if (bools[0] == bools[2]) {return 4;}
                if (bools[1] == bools[2]) {return 5;}
            case 3:
                return 6;
        }
        return -1;
    }

    public static byte[] decode(byte[] corrected) {
        byte[] decoded = new byte[corrected.length / 2];
        byte[] bits = new byte[(corrected.length * 4)];

        int bitNo = 0;

        // For each correct byte isolate the 3rd, 5th, 6th and 7th bit
        for (byte b : corrected) {
            // 3rd bit
            bits[bitNo] = (byte) ((b >> 5) & 1);
            bitNo++;

            // 5th bit
            bits[bitNo] = (byte) ((b >> 3) & 1);
            bitNo++;

            // 6th bit
            bits[bitNo] = (byte) ((b >> 2) & 1);
            bitNo++;

            // 7th bit
            bits[bitNo] = (byte) ((b >> 1) & 1);
            bitNo++;
        }

        // Correlate the bits into bytes
        for (int bitCounter = 0; bitCounter < bits.length; bitCounter++) {
            int byteNo = bitCounter / 8;
            bitNo = bitCounter % 8;

            if (byteNo < decoded.length) {
                decoded[byteNo] = (byte) (decoded[byteNo] | (bits[bitCounter] << (7 - bitNo)));
            }
        }
        return decoded;
    }
}

