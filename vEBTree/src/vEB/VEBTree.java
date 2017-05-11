package vEB;

import java.util.HashMap;

public class VEBTree implements IntegerSet {

    private long min, max;
    private int size, shift;
    private IntegerSet summary;
    private HashMap<Long, IntegerSet> clusters;

    VEBTree(int s) {
        min = max = NO;
        clusters = new HashMap<>();
        size = s;
        shift = (size + 1) / 2;
        if (shift == 1) {
            summary = new SimpleVEBTree(shift);
        } else {
            summary = new VEBTree(shift);
        }

    }

    private static boolean isEmpty(IntegerSet tree) {
        return tree.getMin() == NO;
    }

    private long high(long x) {
        return x >> shift;
    }

    private long low(long x) {
        return x & ((1L << shift) - 1);
    }

    private IntegerSet createChild() {
        if (shift == 1) {
            return new SimpleVEBTree(shift);
        }
        return new VEBTree(shift);
    }

    @Override
    public void add(long x) {
        if (isEmpty(this)) {
            min = max = x;
            return;
        }
        if (x == min) {
            return;
        }
        if (x > max) {
            max = x;
        }
        if (x < min) {
            long tmp = min;
            min = x;
            x = min;
        }
        clusters.putIfAbsent(high(x), createChild());
        if (isEmpty(clusters.get(high(x)))) {
            summary.add(high(x));
        }
        clusters.get(high(x)).add(low(x));
    }

    @Override
    public void remove(long x) {
        if (x == min) {
            if (isEmpty(summary)) {
                min = max = NO;
                return;
            }
            x = clusters.get(summary.getMin()).getMin() | (summary.getMin() << shift);
            min = x;
        }
        clusters.get(high(x)).remove(low(x));
        if (clusters.get(high(x)).getMin() == NO) {
            summary.remove(high(x));
        }
        if (max == x) {
            if (summary.getMin() != NO) {
                max = clusters.get(summary.getMax()).getMax() | (summary.getMax() << shift);
            } else {
                max = min;
            }
        }
    }

    @Override
    public long next(long x) {
        if (isEmpty(this) || x > max) {
            return NO;
        }
        if (x < min) {
            return min;
        }
        clusters.putIfAbsent(high(x), createChild());
        if (!isEmpty(clusters.get(high(x))) && low(x) < clusters.get(high(x)).getMax()) {
            return clusters.get(high(x)).next(low(x)) | (high(x) << shift);
        }
        long nextHigh = summary.next(high(x));
        if (nextHigh != NO) {
            clusters.putIfAbsent(nextHigh, createChild());
            return clusters.get(nextHigh).getMin() | (nextHigh << shift);
        }
        return NO;
    }

    @Override
    public long prev(long x) {
        if (isEmpty(this) || x < min) {
            return NO;
        }
        if (x > max) {
            return max;
        }
        clusters.putIfAbsent(high(x), createChild());
        if (!isEmpty(clusters.get(high(x))) && low(x) > clusters.get(high(x)).getMin()) {
            return clusters.get(high(x)).prev(low(x)) | (high(x) << shift);
        }
        long prevHigh = summary.prev(high(x));
        if (prevHigh != NO) {
            clusters.putIfAbsent(prevHigh, createChild());
            return clusters.get(prevHigh).getMax() | (prevHigh << shift);
        }
        return NO;
    }

    @Override
    public long getMin() {
        return min;
    }

    @Override
    public long getMax() {
        return max;
    }

    public static void main(String[] args) {
        VEBTree tree = new VEBTree(20);
        tree.add(5);
        tree.add(11);
        tree.add(10);
        System.out.println(tree.next(5));
        tree.remove(10);
        System.out.println(tree.next(5));
    }

    private class SimpleVEBTree implements IntegerSet {
        boolean[] a;

        SimpleVEBTree(int s) {
            a = new boolean[1 << s];
        }

        @Override
        public void add(long x) {
            a[(int) x] = true;
        }

        @Override
        public void remove(long x) {
            a[(int) x] = false;
        }

        @Override
        public long next(long x) {
            int current = (int) (x + 1);
            while (current < a.length && !a[current]) {
                current++;
            }
            return current < a.length ? current : NO;
        }

        @Override
        public long prev(long x) {
            int current = (int) (x - 1);
            while (current >= 0 && !a[current]) {
                --current;
            }
            return current >= 0 ? current : NO;
        }

        @Override
        public long getMin() {
            int current = 0;
            while (current < a.length && !a[current]) {
                ++current;
            }
            return current < a.length ? current : NO;
        }

        @Override
        public long getMax() {
            int current = a.length - 1;
            while (current >= 0 && !a[current]) {
                ++current;
            }
            return current < a.length ? current : NO;
        }
    }
}
