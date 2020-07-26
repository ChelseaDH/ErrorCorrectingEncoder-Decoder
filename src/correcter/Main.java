package correcter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

                byte[] encoded = Hamming.encode(bytes);

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

                // Correct and decode data
                byte[] corrected = Hamming.correct(bytes);
                byte[] decoded = Hamming.decode(corrected);

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
