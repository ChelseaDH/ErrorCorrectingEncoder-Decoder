package correcter;

public class Parity{

    public static int calculate(int a, int b, int c) {
        if ((a + b + c) % 2 == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public static boolean check(int a, int b, int c, int parity) {
        return calculate(a, b, c) == parity;
    }
}
