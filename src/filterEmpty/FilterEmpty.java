package filterEmpty;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class FilterEmpty {
    static ForkJoinPool POOL = new ForkJoinPool();

    public static int[] filterEmpty(String[] arr) {
        int[] bitset = mapToBitSet(arr);
        int[] bitsum = ParallelPrefixSum.parallelPrefixSum(bitset);
        int[] result = mapToOutput(arr, bitsum);
        return result;
    }

    public static int[] mapToBitSet(String[] arr) {
    	if (arr.length > 0) { // Make sure not an empty array or we get boned :(
	    	int[] bitsum = new int[arr.length];
	        POOL.invoke(new MapToBitTask(arr, bitsum, 0, arr.length));
	        return bitsum;
    	} else {
    		return new int[0];
    	}
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
    	if (input.length > 0) { // Make sure it's not an empty array or get boned.  :(
	        int[] out = new int[bitsum[bitsum.length - 1]];
	        POOL.invoke(new MapToOutTask(input, bitsum, out, 0, input.length));
	        return out;
    	} else {
    		return new int[0];
    	}
    }
    
    @SuppressWarnings("serial")
	private static class MapToOutTask extends RecursiveAction {
        int[] out; String[] in; int[] bit;
        int lo, hi;
        
        public MapToOutTask(String[] in, int[] bit, int[] out, int lo, int hi) {
            this.in = in;
            this.lo = lo;
            this.hi = hi;
            this.bit = bit;
            this.out = out;
        }
        
        @Override
        protected void compute() {
            if (hi - lo <= 1) {
                if (lo == 0 && bit[lo] != 0) {
                	out[lo] = in[lo].length();
                } else if (lo > 0 && bit[lo] - bit[lo - 1] != 0) {
                	out[bit[lo] - 1] = in[lo].length();
                }
            } else {
	            int mid = lo + (hi - lo) / 2;
	            MapToOutTask left = new MapToOutTask(in, bit, out, lo, mid);
	            MapToOutTask right = new MapToOutTask(in, bit, out, mid, hi);
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