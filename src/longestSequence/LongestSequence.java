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
        int longest = 0;
        for (int i = lo; i < hi; i++) {
            if (arr[i] == val) {
                count++;
            } else {
            	longest = Math.max(longest, count);
            	count = 0;
            }
        }
        return new SequenceRange(lo, hi, longest, hi - lo);
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

            if (leftSeq.matchingOnRight != rightSeq.matchingOnLeft || leftSeq.matchingOnLeft != rightSeq.matchingOnRight) { // Two sequences aren't next to each other
            	if (leftSeq.longestRange >= rightSeq.longestRange) {
            		return leftSeq;
            	} else {
            		return rightSeq;
            	}
            } else { // Left and right are sequences right next to each other
            	if (arr[mid] != val) { // If the middle value of the array is not the value concerned
            		if (leftSeq.longestRange >= rightSeq.longestRange) {
                		return leftSeq;
                	} else {
                		return rightSeq;
                	}
            	} else { // We have to check if the sequence lengths should be added together or not
            		int leftLongest = leftSeq.longestRange;
            		int rightLongest = rightSeq.longestRange;
            		int leftLen = leftSeq.sequenceLength;
            		int rightLen = rightSeq.sequenceLength;
            		
            		if (leftLongest + rightLongest == leftLen + rightLen) { // If both seq only match value
            			return new SequenceRange(lo, hi, leftLongest + rightLongest, leftLen + rightLen);
            		} else { // Have to check if the consecutive ones in middle is greater or if left/right seq are
            			SequenceRange totalSeq = sequential(arr, lo, hi, val);
            			if (totalSeq.longestRange > leftLongest && totalSeq.longestRange > rightLongest) {
            				return totalSeq;
            			} else {
            				if (leftSeq.longestRange >= rightSeq.longestRange) {
                        		return leftSeq;
                        	} else {
                        		return rightSeq;
                        	}
            			}
            		}
            	}
            }
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