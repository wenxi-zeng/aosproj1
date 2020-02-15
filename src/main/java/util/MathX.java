package util;

import java.util.*;

public class MathX {
    private static final long MAX_VALUE = 0xFFFFFFFFL;
    private static final double MAX_NODE = 15359.0;

    private static Random random = new Random(100);

    public static Queue<Integer> nonrepeatRandom(int bound, int size, List<Integer> exclude) {
        Set<Integer> s = new HashSet<>(exclude);
        Queue<Integer> result = new LinkedList<>();

        for(int i = 0; i < size; i++){
            while(true) {
                int num = random.nextInt(bound);
                if (!s.contains(num)) {
                    s.add(num);
                    result.add(num);
                    break;
                }
            }
        }

        return result;
    }

    public static Queue<Integer> nonrepeatRandom(int bound, int size) {
        return nonrepeatRandom(bound, size, new ArrayList<>());
    }

    public static int nextInt(int bound) {
        return (int)(random.nextDouble() * (bound));
    }

    public static int nextInt(int min, int max) {
        return min + (int)(random.nextDouble() * ((max - min) + 1));
    }

    public static <T> void shuffle(T[] array) {
        int count = array.length;
        for (int i = count; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
    }

    private static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static int positiveHash(int hash) {
        return hash & 0x7fffffff;
    }

    private static long add(long val, long add) {
        return (val + add) & MAX_VALUE;
    }

    /**
     * Do subtraction and turn into 4 bytes.
     */
    private static long subtract(long val, long subtract) {
        return (val - subtract) & MAX_VALUE;
    }

    /**
     * Left shift val by shift bits and turn in 4 bytes.
     */
    private static long xor(long val, long xor) {
        return (val ^ xor) & MAX_VALUE;
    }

    /**
     * Left shift val by shift bits.  Cut down to 4 bytes.
     */
    private static long leftShift(long val, int shift) {
        return (val << shift) & MAX_VALUE;
    }

    public static double rushHash(String s1,int r,String cid){

        long a = s1.hashCode();
        long b = r & MAX_VALUE;
        long c = cid.hashCode();

        a = subtract(a, b); a = subtract(a, c); a = xor(a, c >> 13);
        b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 8));
        c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 13));
        a = subtract(a, b); a = subtract(a, c); a = xor(a, (c >> 12));
        b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 16));
        c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 5));
        a = subtract(a, b); a = subtract(a, c); a = xor(a, (c >> 3));
        b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 10));
        c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 15));


        return (c%MAX_NODE)/MAX_NODE;
    }
}
