import java.io.*;
import java.util.Scanner;
import java.math.BigInteger;
import parcs.*;

public class MonteCarlo implements AM {
    private static long startTime = 0;

    public static void main(String[] args) throws Exception {
        System.err.println("Preparing...");

        if (args.length != 1) {
            System.err.println("Number of workers not specified");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);

        task curtask = new task();
        curtask.addJarFile("MonteCarlo.jar");
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

        System.err.println("Forwarding parts to workers...");
        startTime = System.nanoTime();
        channel[] channels = new channel[n];
        for (int i = 0; i < n; i++) {
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("MonteCarlo");
            c.write(N);
            c.write(x1);
            c.write(x2);
            c.write(y1);
            c.write(y2);
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
        int N = (int) info.parent.readObject();
        double x1 = (double) info.parent.readObject();
        double x2 = (double) info.parent.readObject();
        double y1 = (double) info.parent.readObject();
        double y2 = (double) info.parent.readObject();

        int num = mymap(N, x1, x2, y1, y2);

        info.parent.write(num);
    }

    public static int mymap(int N1, double x1, double x2, double y1, double y2) {
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
        return num;
    }

    public static double foo(double x) {
        return 1 / (Math.pow(x, 5) + 1);
    }
}
