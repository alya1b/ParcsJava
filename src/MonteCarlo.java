import java.io.*;
import java.util.Scanner;
import java.math.BigInteger;
import parcs.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.math.BigInteger;
import java.util.Random;

public class MonteCarlo implements AM {
    private static long startTime = 0;
    private static final BigInteger MODULE = new BigInteger("2147483647");
    private static final BigInteger BASE =  new BigInteger("31");
	public static double foo(double x) {
        return 1 / (Math.pow(x, 5) + 1);
        }
	 
	public static int computeHash(String str) {
		
		int N1 = 10;
       		double x1 = 0.0;
        	double x2 = 2.0;
        	double y1 = 0.0;
        	double y2 = 2.0;
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


    public static void main(String[] args) throws Exception {
    	
    	
        System.err.println("Preparing...");
        
        if (args.length != 1) {
            System.err.println("Nnumber of workers not specified");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);

        task curtask = new task();
        curtask.addJarFile("MonteCarlo.jar");
        AMInfo info = new AMInfo(curtask, null);

        System.err.println("Reading input...");
       
        
	String S = "";
	try{
		Scanner sc = new Scanner(new File(info.curtask.findFile("input.txt")));
		S = sc.nextLine();
	}
	catch (IOException e) {e.printStackTrace(); return;}

	String[] inputs = S.split(" ");
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
        
        int len = S.length();
        int sub_len = (len + n - 1) / n;

        System.err.println("Forwarding parts to workers...");
       startTime = System.nanoTime();
        channel[] channels = new channel[n];
        for (int i = 0; i < n; i++) {
            String substring = "";
	    if (i * sub_len < S.length()) {
		substring = S.substring(i * sub_len, 
            		Math.min((i * sub_len + sub_len), S.length()));
		}
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("MonteCarlo");
            c.write(substring);
            channels[i] = c;
        }

        System.err.println("Getting results");
      
        int[] sub_hash = new int[n];
        for (int i = 0; i < n; i++) {
        	sub_hash[i] = channels[i].readInt();
        }

        System.err.println("Calculation of the result");
     
        //BigInteger hash = resultСalculation(sub_hash, sub_len);
        int hash = 0;
 	long endTime = System.nanoTime();
	
        System.out.println("Result: " + Integer.toString(hash));
       
        
        long timeElapsed = endTime - startTime;
        double seconds = timeElapsed / 1_000_000_000.0;
        System.err.println("Time passed: " + seconds + " seconds.");
        
        
        curtask.end();
    }


    public void run(AMInfo info) {
     
        String substring = (String)info.parent.readObject();
        int num = computeHash(substring);
        info.parent.write(num);
  
    }

    public static BigInteger resultСalculation(BigInteger[] subhash, int sublen) {
        
        BigInteger delt = BigInteger.ONE;
        BigInteger step = BASE.modPow(BigInteger.valueOf(sublen), MODULE);
        BigInteger output = BigInteger.ZERO;
        
        for (BigInteger x : subhash) {
	     System.out.println(x.toString());
            output = output.add(x.multiply(delt).mod(MODULE)).mod(MODULE);
            delt = delt.multiply(step).mod(MODULE);
        }
       return output;
    }
}
