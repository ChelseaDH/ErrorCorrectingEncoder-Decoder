package correcter;

public class Hamming {

    public byte[] encode(byte[] bytes) {
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
}

