package longestSequence;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LongestSequence {
	private static final ForkJoinPool POOL = new ForkJoinPool();
    public static int getLongestSequence(int val, int[] arr, int sequentialCutoff) {
    	SequenceRange seq = POOL.invoke(new LongestSequenceTask(arr, 0, arr.length, val, sequentialCutoff));
    	return seq.longestRange;
    }

    public static SequenceRange sequential(int[] arr, int lo, int hi, int val) {
        int count = 0;
        int left = 0;
        int right = 0;
        int longest = 0;
        boolean countEdge = true;
        int pos = lo;
        while (pos < hi && countEdge) {
        	if (arr[pos] == val) {
        		count++;
        	} else {
        		left = count;
        		countEdge = false;
        	}
        	pos++;
        }
        left = Math.max(left, count);
        pos = hi - 1;
        count = 0;
        countEdge = true;
        while (pos >= lo && countEdge) {
        	if (arr[pos] == val) {
        		count++;
        	} else {
        		right = count;
        		countEdge = false;
        	}
        	pos--;
        }
        right = Math.max(right, count);
        count = 0;
        for (int i = lo; i < hi; i++) {
        	if (arr[i] == val) {
        		count++;
        	} else {
        		longest = Math.max(longest, count);
        		count = 0;
        	}
        }
        longest = Math.max(longest, count);
        return new SequenceRange(left, right, longest, hi - lo);
    }
    
    @SuppressWarnings("serial")
	private static class LongestSequenceTask extends RecursiveTask<SequenceRange> {
        int[] arr;
        int lo, hi; int val; int sequentialCutoff;
        
        public LongestSequenceTask(int[] arr, int lo, int hi, int val, int sequentialCutoff) {
            this.arr = arr;
            this.lo = lo;
            this.hi = hi;
            this.val = val;
            this.sequentialCutoff = sequentialCutoff;
        }
        
        @Override
        protected SequenceRange compute() {
            if (hi - lo <= sequentialCutoff) {
                return sequential(arr, lo, hi, val);
            }

            int mid = lo + (hi - lo) / 2;

            LongestSequenceTask left = new LongestSequenceTask(arr, lo, mid, val, sequentialCutoff);
            LongestSequenceTask right = new LongestSequenceTask(arr, mid, hi, val, sequentialCutoff);
            
            left.fork();

            SequenceRange rightSeq = right.compute();
            SequenceRange leftSeq = left.join();

            int newLeft = leftSeq.matchingOnLeft;
    		int newRight = rightSeq.matchingOnRight;
    		int middle = leftSeq.matchingOnRight + rightSeq.matchingOnLeft;
    		int newLength = leftSeq.sequenceLength + rightSeq.sequenceLength;
    		if (leftSeq.matchingOnLeft == leftSeq.sequenceLength) {
    			newLeft += rightSeq.matchingOnLeft;
    		}
    		if (rightSeq.matchingOnRight == rightSeq.sequenceLength) {
    			newRight += leftSeq.matchingOnRight;
    		}
            int longest = newLeft;
            longest = Math.max(longest, newRight);
            longest = Math.max(longest, middle);
            longest = Math.max(longest, leftSeq.longestRange);
            longest = Math.max(longest, rightSeq.longestRange);
            return new SequenceRange(newLeft, newRight, longest, newLength);
        }
        
    }
    
    private static void usage() {
        System.err.println("USAGE: LongestSequence <number> <array> <sequential cutoff>");
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
            System.out.println(getLongestSequence(val, arr, Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            usage();
        }
    }
}