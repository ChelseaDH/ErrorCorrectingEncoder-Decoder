package correcter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {
        Random r = new Random();

        FileInputStream inputStream = null;

//        System.out.print("Write a mode: ");
//        String mode = s.nextLine();
//        System.out.println();

        // ENCODE MODE
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

        // Print to console
        System.out.println("send.txt:");
        printText(bytes);
        printHex(bytes);
        printBin(bytes, "hex view", true);
        System.out.println();

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

        // Create the encoded file and write to it
        FileOutputStream outputStream = new FileOutputStream("encoded.txt");
        for (byte b : encoded) {
            outputStream.write(b);
        }
        outputStream.close();

        // Print to the console
        System.out.println("encoded.txt:");
        printBin(encoded, "expand", false);
        printBin(encoded, "parity", true);
        printHex(encoded);
        System.out.println();

        // SEND MODE
        // Open the encoded text
        try {
            inputStream = new FileInputStream("send.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
            System.exit(1);
        }

        // Grab data from input
        bytes = inputStream.readAllBytes();

        // Print to the console
        System.out.println("encoded.txt:");
        printHex(bytes);
        printBin(bytes, "bin view", true);
        System.out.println();

        // Add errors
        // Open file to write the encoded bytes to
        outputStream = new FileOutputStream("received.txt");
        // Iterate through the input
        // Complete a bit shift based on a random int
        // Add the resulting byte to the list
        int byteAsInt = inputStream.read();
        while (byteAsInt != -1) {
            int shift = 1 << r.nextInt(8);
            outputStream.write((byte) (byteAsInt ^ shift));
            byteAsInt = inputStream.read();
        }
        inputStream.close();
        outputStream.close();

        // Open the received to print values
        inputStream = new FileInputStream("received.txt");

        // Grab data from input
        bytes = inputStream.readAllBytes();
        inputStream.close();

        // Print to the console
        System.out.println("received.txt");
        printBin(bytes, "bin view", true);
        printHex(bytes);
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
}
