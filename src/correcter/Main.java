package correcter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        FileInputStream inputStream = null;

        System.out.print("Write a mode: ");
        String mode = scanner.nextLine();
        System.out.println();

        switch (mode) {
            case "encode":
                // Create a new file reader to read the file
                try {
                    inputStream = new FileInputStream("send.txt");
                } catch (FileNotFoundException e) {
                    System.out.println("Error: File not found");
                    System.exit(1);
                }

                // Open file and read data
                byte[] bytes = inputStream.readAllBytes();
                inputStream.close();

                byte[] encoded = encode(bytes);

                // Create the encoded file and write to it
                FileOutputStream outputStream = new FileOutputStream("encoded.txt");
                outputStream.write(encoded);
                outputStream.close();

                // Print to the console
                System.out.println("send.txt:");
                printText(bytes);
                printHex(bytes);
                printBin(bytes, "bin view", true);
                System.out.println();

                System.out.println("encoded.txt:");
                printBin(encoded, "expand", false);
                printBin(encoded, "parity", true);
                printHex(encoded);
                break;

            case "send":
                // Open the encoded text
                try {
                    inputStream = new FileInputStream("encoded.txt");
                } catch (FileNotFoundException e) {
                    System.out.println("Error: File not found");
                    System.exit(1);
                }

                // Grab data from input
                bytes = inputStream.readAllBytes();
                inputStream.close();

                // Print current state to the console
                System.out.println("encoded.txt:");
                printHex(bytes);
                printBin(bytes, "bin view", true);
                System.out.println();

                // Add errors
                Errors.addRandom(bytes);

                // Open file to write the encoded bytes to
                outputStream = new FileOutputStream("received.txt");
                outputStream.write(bytes);
                outputStream.close();

                // Print output to console
                System.out.println("received.txt");
                printBin(bytes, "bin view", true);
                printHex(bytes);
                break;

            case "decode":
                // Open the received text
                try {
                    inputStream = new FileInputStream("received.txt");
                } catch (FileNotFoundException e) {
                    System.out.println("Error: File not found");
                    System.exit(1);
                }

                // Grab data from input
                bytes = inputStream.readAllBytes();
                inputStream.close();

                byte[] corrected = correct(bytes);
                byte[] decoded = decode(corrected);

                // Open file to write the decoded bytes to
                outputStream = new FileOutputStream("decoded.txt");
                outputStream.write(decoded);
                outputStream.close();

                // Print to the console
                System.out.println("received.txt:");
                printHex(bytes);
                printBin(bytes, "bin view", true);
                System.out.println();

                System.out.println("decoded.txt");
                printBin(corrected, "correct", true);
                printBin(decoded, "decode", true);
                printHex(decoded);
                printText(decoded);
                break;
        }
    }

    private static byte[] encode(byte[] bytes) {
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
            int parity1 = calculateParity(currentBits[0], currentBits[1], currentBits[3]);
            int parity2 = calculateParity(currentBits[0], currentBits[2], currentBits[3]);
            int parity3 = calculateParity(currentBits[1], currentBits[2], currentBits[3]);

            int newByte = (parity1 << 7 | parity2 << 6 | currentBits[0] << 5 | parity3 << 4
                    | currentBits[1] << 3 | currentBits[2] << 2 | currentBits[3] << 1);

            encoded[bitCounter / 4] = (byte) newByte;
        }
        return encoded;
    }

    private static byte[] correct(byte[] bytes) {
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
            isParityCorrect[0] = checkParity(bits[2], bits[4], bits[6], bits[0]);
            isParityCorrect[1] = checkParity(bits[2], bits[5], bits[6], bits[1]);
            isParityCorrect[2] = checkParity(bits[4], bits[5], bits[6], bits[3]);

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

    private static byte[] decode(byte[] corrected) {
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

    public static void printText(byte[] input) {
        System.out.printf("text view: %s\n", new String(input));
    }

    public static void printHex(byte[] input) {
        System.out.print("hex view: ");
        for (int i = 0; i < input.length - 1; i++) {
            System.out.printf("%02X ", input[i]);
        }
        System.out.printf("%02X\n", input[input.length - 1]);
    }

    public static void printBin(byte[] input, String flag, boolean full) {
        System.out.printf("%s: ", flag);
        if (full) {
            for (int i = 0; i < input.length - 1; i++) {
                System.out.printf("%s ", IntAsBin(input[i]));
            }
            System.out.printf("%s\n", IntAsBin(input[input.length - 1]));
        } else {
            for (int i = 0; i < input.length - 1; i++) {
                System.out.printf("%s ", IntAsBinExpanded(input[i]));
            }
            System.out.printf("%s\n", IntAsBinExpanded(input[input.length - 1]));
        }
    }

    private static String IntAsBin(int input) {
        return String.format("%8s", Integer.toString(input & 0xff, 2)).replace(" ", "0");
    }

    private static String IntAsBinExpanded(int input) {
        return IntAsBin(input).replaceAll("..(.).(.{3}).", "..$1.$2.");
    }
}
