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
                System.out.println();
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
                addErrors(bytes);

                // Open file to write the encoded bytes to
                outputStream = new FileOutputStream("received.txt");
                outputStream.write(bytes);
                outputStream.close();

                // Print output to console
                System.out.println("received.txt");
                printBin(bytes, "bin view", true);
                printHex(bytes);
                System.out.println();
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

                outputStream = new FileOutputStream("decoded.txt");

                byte[] removed = new byte[(corrected.length * 3) / 8];
                for (int byteNo = 0; byteNo < removed.length; byteNo++) {
                    removed[byteNo] = decoded[byteNo];
                    outputStream.write(removed[byteNo]);
                }
                outputStream.close();

                // Print to the console
                System.out.println("received.txt:");
                printHex(bytes);
                printBin(bytes, "bin view", true);
                System.out.println();

                System.out.println("decoded.txt");
                printBin(corrected, "correct", true);
                printBin(decoded, "decode", true);
                printBin(removed, "remove", true);
                printHex(removed);
                printText(removed);
                break;
        }
    }

    private static byte[] decode(byte[] corrected) {
        byte[] decoded = new byte[((corrected.length * 3) / 8) + 1];
        byte[] bits = new byte[(corrected.length * 3)];

        int bitNo = 0;

        // For each correct byte isolate the 1st, 3rd and 5th bit
        for (byte b : corrected) {
            // 1st bit
            bits[bitNo] = (byte) ((b >> 7) & 1);
            bitNo++;

            // 3rd bit
            bits[bitNo] = (byte) ((b >> 5) & 1);
            bitNo++;

            // 5th bit
            bits[bitNo] = (byte) ((b >> 3) & 1);
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

    private static byte[] correct(byte[] bytes) {
        // Correct the error in each byte
        byte[] corrected = new byte[bytes.length];

        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {

            byte[] bits = new byte[8];

            // Obtain the individual bits
            for (int j = 0; j < 8; j++) {
                bits[j] = (byte) ((bytes[byteIndex] >> (7 - j)) & 1);
            }

            // Check each pair for the error and correct it
            int pairNo = 1;
            for (int j = 0; j < 8; j += 2) {
                if ((bits[j] ^ bits[j + 1]) == 1 && pairNo < 4) {
                    byte correctBit = 0;
                    switch (pairNo) {
                        case 1:
                            correctBit = correctPair(bits[7], bits[3], bits[5]);
                            break;
                        case 2:
                            correctBit = correctPair(bits[7], bits[1], bits[5]);
                            break;
                        case 3:
                            correctBit = correctPair(bits[7], bits[1], bits[3]);
                            break;
                    }
                    bits[j] = correctBit;
                    bits[j + 1] = correctBit;
                    break;
                }
                pairNo++;
            }

            // Put the byte back together in corrected
            int correctedByte = 0;
            for (int j = 0; j < 8; j++) {
                correctedByte = correctedByte | (bits[j] << (7 - j));
            }

            corrected[byteIndex] = (byte) correctedByte;
        }
        return corrected;
    }

    private static void addErrors(byte[] bytes) {
        Random r = new Random();
        // Iterate through the input
        // Complete a bit shift based on a random int
        // Add the resulting byte to the file
        for (int i = 0; i < bytes.length; i++) {
            int shift = 1 << r.nextInt(8);
            bytes[i] ^= shift;
        }
    }

    private static byte[] encode(byte[] bytes) {
        // Encode the text
        int bitCounter = 0;
        int numberOfBits = bytes.length * 8;
        byte[] encoded = new byte[(numberOfBits / 3) + (numberOfBits % 3) - 1];

        int[] currentBits;

        int byteNo, bitNo;
        while (bitCounter < numberOfBits) {
            currentBits = new int[]{0, 0, 0};
            for (int i = 0; i < 3; i++) {
                byteNo = (bitCounter + i) / 8;
                bitNo = (bitCounter + i) % 8;

                if (byteNo < bytes.length) {
                    currentBits[i] = (bytes[byteNo] >> (7 - bitNo)) & 1;
                }
            }

            int parity = currentBits[0] ^ currentBits[1] ^ currentBits[2];
            int newByte = currentBits[0] << 7 | currentBits[1] << 5 | currentBits[2] << 3 | parity << 1;
            encoded[bitCounter / 3] = (byte) (newByte | (newByte >> 1));
            bitCounter += 3;
        }
        return encoded;
    }

    public static void printText(byte[] input) {
        System.out.printf("text view: %s\n", new String(input));
    }

    public static void printHex(byte[] input) {
        System.out.print("hex view: ");
        for (byte b : input) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }

    public static void printBin(byte[] input, String flag, boolean full) {
        System.out.printf("%s: ", flag);
        if (full) {
            for (byte b : input) {
                System.out.print(IntAsBin(b) + " ");
            }
        } else {
            for (byte b : input) {
                System.out.print(IntAsBinExpanded(b) + " ");
            }
        }
        System.out.println();
    }

    private static String IntAsBin(int input) {
        return String.format("%8s", Integer.toString(input & 0xff, 2)).replace(" ", "0");
    }

    private static String IntAsBinExpanded(int input) {
        return IntAsBin(input).replaceAll("(.{6})..", "$1..");
    }

    private static byte correctPair(int parity, int a, int b) {
        if ((parity == 0 && (a == 1 ^ b == 1)) || (parity == 1 && (a == b))) {
            return 1;
        }

        return 0;
    }
}
