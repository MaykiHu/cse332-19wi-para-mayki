package filterEmpty;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;


public class FilterEmpty {
    static ForkJoinPool POOL = new ForkJoinPool();

    public static int[] filterEmpty(String[] arr) {
        int[] bitset = mapToBitSet(arr);
        //System.out.println(java.util.Arrays.toString(bitset));
        int[] bitsum = ParallelPrefixSum.parallelPrefixSum(bitset);
        //System.out.println(java.util.Arrays.toString(bitsum));
        int[] result = mapToOutput(arr, bitsum);
        return result;
    }

    public static int[] mapToBitSet(String[] arr) {
        return POOL.invoke(new MapToBitTask(arr, new int[arr.length], 0, arr.length));
    }
    
    @SuppressWarnings("serial")
	private static class MapToBitTask extends RecursiveTask<int[]> {
        int[] out; String[] in;
        int lo, hi;
        
        public MapToBitTask(String[] in, int[] out, int lo, int hi) {
            this.in = in;
            this.out = out;
            this.lo = lo;
            this.hi = hi;
        }
        
        @Override
        protected int[] compute() {
            if (hi - lo <= 0) {
                for (int i = lo; i < hi; i++) {
                	if (in[i].length() > 0) {
                		out[i] = 1;
                	} // else, out[i] = 0, by default
                }
                return out;
            }

            int mid = lo + (hi - lo) / 2;

            MapToBitTask left = new MapToBitTask(in, out, lo, mid);
            MapToBitTask right = new MapToBitTask(in, out, mid, hi);
            
            left.fork();
            right.compute();
            left.join();

            return out;
        }
        
    }
    
    public static int[] mapToOutput(String[] input, int[] bitsum) {
        POOL.invoke(new MapToOutTask(input, bitsum, new int[bitsum[bitsum.length - 1]], 0, bitsum.length));
    }
    
    @SuppressWarnings("serial")
	private static class MapToOutTask extends RecursiveTask<int[]> {
        int[] out; String[] in; int[] bit;
        int lo, hi;
        
        public MapToOutTask(String[] in, int[] bit, int[] out, int lo, int hi) {
            this.in = in;
            this.out = out;
            this.lo = lo;
            this.hi = hi;
            this.bit = bit;
        }
        
        @Override
        protected int[] compute() {
            if (hi - lo <= 0) {
                for (int i = lo + 1; i < hi; i++) {
                	if (bit[i] - bit[i - 1] > 0) {
                		out[i] = in[i - 1].length();
                	} 
                }
                return out;
            }

            int mid = lo + (hi - lo) / 2;

            MapToOutTask left = new MapToOutTask(in, bit, out, lo, mid);
            MapToOutTask right = new MapToOutTask(in, bit, out, mid, hi);
            
            left.fork();
            right.compute();
            left.join();

            return out;
        }
        
    }

    private static void usage() {
        System.err.println("USAGE: FilterEmpty <String array>");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
        }

        String[] arr = args[0].replaceAll("\\s*", "").split(",");
        System.out.println(Arrays.toString(filterEmpty(arr)));
    }
}