package hasOver;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class HasOver {
	private static final ForkJoinPool POOL = new ForkJoinPool();
    public static boolean hasOver(int val, int[] arr, int sequentialCutoff) {
        return POOL.invoke(new HasOverTask(arr, 0, arr.length, val, sequentialCutoff));
    }
    
    public static boolean sequential(int[] arr, int lo, int hi, int val) {
        for (int i = lo; i < hi; i++) {
            if (arr[i] > val) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("serial")
	private static class HasOverTask extends RecursiveTask<Boolean> {
        int[] arr;
        int lo, hi; int val; int sequentialCutoff;
        
        public HasOverTask(int[] arr, int lo, int hi, int val, int sequentialCutoff) {
            this.arr = arr;
            this.lo = lo;
            this.hi = hi;
            this.val = val;
            this.sequentialCutoff = sequentialCutoff;
        }
        
        @Override
        protected Boolean compute() {
            if (hi - lo <= sequentialCutoff) {
                return sequential(arr, lo, hi, val);
            }

            int mid = lo + (hi - lo) / 2;

            HasOverTask left = new HasOverTask(arr, lo, mid, val, sequentialCutoff);
            HasOverTask right = new HasOverTask(arr, mid, hi, val, sequentialCutoff);
            
            left.fork();

            Boolean rightResult = right.compute();
            Boolean leftResult = left.join();

            return leftResult || rightResult;
        }
        
    }

    private static void usage() {
        System.err.println("USAGE: HasOver <number> <array> <sequential cutoff>");
        System.exit(2);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            usage();
        }

        int val = 0;
        int[] arr = null;

        try {
            val = Integer.parseInt(args[0]); 
            String[] stringArr = args[1].replaceAll("\\s*",  "").split(",");
            arr = new int[stringArr.length];
            for (int i = 0; i < stringArr.length; i++) {
                arr[i] = Integer.parseInt(stringArr[i]);
            }
            System.out.println(hasOver(val, arr, Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            usage();
        }
        
    }
}
