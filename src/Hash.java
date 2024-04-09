import java.io.*;
import java.util.Scanner;
import java.math.BigInteger;
import parcs.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.math.BigInteger;
import java.util.Random;

public class Hash implements AM {
    private static long startTime = 0;

    public static void main(String[] args) throws Exception {
        System.err.println("Preparing...");

        if (args.length != 1) {
            System.err.println("Number of workers not specified");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);

        task curtask = new task();
        curtask.addJarFile("Hash.jar");
        AMInfo info = new AMInfo(curtask, null);

        System.err.println("Reading input...");

        String inputFileName = "input.txt"; // Input file name
        String input = "";
        try {
            Scanner scanner = new Scanner(new File(inputFileName));
            input = scanner.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String[] inputs = input.split(" ");
        int N = Integer.parseInt(inputs[0]);
        double x1 = Double.parseDouble(inputs[1]);
        double x2 = Double.parseDouble(inputs[2]);
        double y1 = Double.parseDouble(inputs[3]);
        double y2 = Double.parseDouble(inputs[4]);

        System.err.println("N: " + N);
        System.err.println("x1: " + x1);
        System.err.println("x2: " + x2);
        System.err.println("y1: " + y1);
        System.err.println("y2: " + y2);

        System.err.println("Forwarding parts to workers...");
        startTime = System.nanoTime();
        channel[] channels = new channel[n];
        int remainder = N % n;
        int chunkSize = N / n;
        for (int i = 0; i < n; i++) {
            int size = chunkSize + ((i < remainder) ? 1 : 0);
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("Hash");
            c.write(new double[] {size, x1, x2, y1, y2}); // Write all values in one array
            channels[i] = c;
        }

        System.err.println("Getting results");

        int result = 0;
        for (int i = 0; i < n; i++) {
            result += (int) channels[i].readObject();
        }

        System.err.println("Calculation of the result");

        double integral = (double) result * (x2 - x1) * (y2 - y1) / N;

        long endTime = System.nanoTime();

        System.out.println("Result: " + integral);

        long timeElapsed = endTime - startTime;
        double seconds = timeElapsed / 1_000_000_000.0;
        System.err.println("Time passed: " + seconds + " seconds.");

        curtask.end();
    }

    public void run(AMInfo info) {
        System.err.println("Getting part from parent...");
        double[] params = (double[]) info.parent.readObject();
        int N1 = (int) params[0];
        double x1 = params[1];
        double x2 = params[2];
        double y1 = params[3];
        double y2 = params[4];

        Random random = new Random();
        int num = 0;
        for (int i = 0; i < N1; i++) {
            double x = random.nextDouble() * (x2 - x1) + x1;
            double y = random.nextDouble() * (y2 - y1) + y1;
            double fun = foo(x);
            if (0 <= y && y <= fun) {
                num += 1;
            } else if (0 >= y && y >= fun) {
                num -= 1;
            }
        }

        info.parent.write(num);
    }

    public static double foo(double x) {
        return 1 / (Math.pow(x, 5) + 1);
    }
}
