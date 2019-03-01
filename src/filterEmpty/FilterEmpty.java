package filterEmpty;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


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
    	int[] bitsum = new int[arr.length];
        POOL.invoke(new MapToBitTask(arr, bitsum, 0, arr.length));
        return bitsum;
    }
    
    @SuppressWarnings("serial")
	private static class MapToBitTask extends RecursiveAction {
        int[] out; String[] in;
        int lo, hi;
        
        public MapToBitTask(String[] in, int[] out, int lo, int hi) {
            this.in = in;
            this.out = out;
            this.lo = lo;
            this.hi = hi;
        }
        
        @Override
        protected void compute() {
            if (hi - lo <= 1) {
               	if (in[lo].length() > 0) {
                	out[lo] = 1;
                } // else, out[i] = 0, by default
            } else {
	            int mid = lo + (hi - lo) / 2;
	
	            MapToBitTask left = new MapToBitTask(in, out, lo, mid);
	            MapToBitTask right = new MapToBitTask(in, out, mid, hi);
	            
	            left.fork();
	            right.compute();
	            left.join();
            }
        }
    }
    
    public static int[] mapToOutput(String[] input, int[] bitsum) {
        int[] out = new int[bitsum[bitsum.length - 1]];
        POOL.invoke(new MapToOutTask(input, bitsum, 0, input.length));
        return out;
    }
    
    @SuppressWarnings("serial")
	private static class MapToOutTask extends RecursiveAction {
        int[] out; String[] in; int[] bit;
        int lo, hi;
        
        public MapToOutTask(String[] in, int[] bit, int lo, int hi) {
            this.in = in;
            this.lo = lo;
            this.hi = hi;
            this.bit = bit;
        }
        
        @Override
        protected void compute() {
            if (hi - lo <= 1) {
                if (lo == 0 && bit[lo] != 0) {
                	out[lo] = in[lo].length();
                } else if (lo > 0 && bit[lo] - bit[lo - 1] != 0) {
                	out[lo] = in[lo].length();
                }
            } else {
	            int mid = lo + (hi - lo) / 2;
	
	            MapToOutTask left = new MapToOutTask(in, bit, lo, mid); //0, 1 // 0, 1
	            MapToOutTask right = new MapToOutTask(in, bit, mid, hi);//1, 2 // 1, 3 -> 1, 2  2, 3
	            
	            left.fork();
	            right.compute();
	            left.join();
            }
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