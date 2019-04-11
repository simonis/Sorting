package io.simonis;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class QuickSort {
  private final static boolean DEBUG = Boolean.getBoolean("DEBUG");

  public static void quicksort(int[] a) {
    quicksort(a, 0, a.length - 1);
  }

  public static void quicksort(int[] a, int lo, int hi) {
    if (lo < hi) {
      int p = partition(a, lo, hi);
      quicksort(a, lo, p);
      quicksort(a, p+1, hi);
    }
  }

  public static int partition(int[] a, int lo, int hi) {
    int pivot = a[(lo + hi) / 2];
    while (lo <= hi) {
      while (a[lo] < pivot) lo++;
      while (a[hi] > pivot) hi--;
      if (lo <= hi) swap(a, lo++, hi--);
    }
    return lo - 1;
  }

  public static void swap(int[] a, int lo, int hi) {
    int tmp = a[lo];
    a[lo] = a[hi];
    a[hi] = tmp;
  }

  static class ParallelQuickSort extends RecursiveAction {
    final int[] a;
    final int lo, hi;
    // Array slices smaller than 'THRESHOLD' won't be sorted in parallel any more.
    final int THRESHOLD = Integer.getInteger("THRESHOLD", 256);

    public ParallelQuickSort(int[] a, int lo, int hi) {
      this.a = a;
      this.lo = lo;
      this.hi = hi;
    }

    @Override
    protected void compute() {
      if (hi - lo <= THRESHOLD) {
        quicksort(a, lo, hi);
      }
      else {
        int p = partition(a, lo, hi);
        invokeAll(new ParallelQuickSort(a, lo, p),
                  new ParallelQuickSort(a, p+1, hi));
      }
    }
  }

  static ForkJoinPool fjp;

  public static void quicksort_p(int[] a) {
    fjp.invoke(new ParallelQuickSort(a, 0, a.length - 1));
  }

  public static void test() {
    int[][] aa = {
      { 0 },
      { 0, 0 },
      { 0, 1 },
      { 1, 0 },
      { 0, 0, 0 },
      { 1, 0, 0 },
      { 0, 1, 0 },
      { 0, 0, 1 },
      { 0, 1, 2 },
      { 0, 2, 1 },
      { 1, 0, 2 },
      { 1, 2, 0 },
      { 2, 0, 1 },
      { 2, 1, 0 },
      { 0, 0, 0, 0 },
      { 0, 0, 1, 1 },
      { 0, 1, 0, 1 },
      { 1, 0, 0, 1 },
      { 0, 1, 0, 1, 0, 0, 0, 1 },
      { 0, 1, 2, 3 }, { 0, 1, 3, 2 }, { 0, 3, 1, 2 }, { 3, 0, 1, 2 },
      { 0, 2, 1, 3 }, { 0, 2, 3, 1 }, { 0, 3, 2, 1 }, { 3, 0, 2, 1 },
      { 1, 0, 2, 3 }, { 1, 0, 3, 2 }, { 1, 3, 0, 2 }, { 3, 1, 0, 2 },
      { 1, 2, 0, 3 }, { 1, 2, 3, 0 }, { 1, 3, 2, 0 }, { 3, 1, 2, 0 },
      { 2, 0, 1, 3 }, { 2, 0, 3, 1 }, { 2, 3, 0, 1 }, { 3, 2, 0, 1 },
      { 2, 1, 0, 3 }, { 2, 1, 3, 0 }, { 2, 3, 1, 0 }, { 3, 2, 1, 0 }};
    for (int i = 0; i < aa.length; i++ ) {
      int[] a1 = aa[i].clone();
      int[] a2 = aa[i].clone();
      Arrays.sort(a1);
      if (DEBUG) System.out.print("# " + Arrays.toString(a2) + " -> ");
      quicksort(a2);
      if (DEBUG) System.out.println(Arrays.toString(a2));
      assert Arrays.equals(a1, a2);
    }
    System.out.println("# Test finished");
  }

  public static void warmup() {
    int[] ra = new int[10_000];
    Random R = new Random();
    for (int i = 0; i < 10_000; i++) {
      for (int j = 0; j < ra.length; j++) {
        ra[j] = R.nextInt();
      }
      int[] c = ra.clone();
      int[] ra2 = ra.clone();
      Arrays.parallelSort(c);
      quicksort(ra);
      assert Arrays.equals(c, ra);
      quicksort(ra2);
      assert Arrays.equals(c, ra2);
    }
    System.out.println("# Warmup finished");
  }

  // Base size of arrays to sort
  private static final int BASE_SIZE = Integer.getInteger("BASE_SIZE", 1024);
  // Number of different array sizes to sort. Sizes will range from 'BASE_SIZE' to 'ITERATIONS' * 'BASE_SIZE'
  private static final int ITERATIONS = Integer.getInteger("ITERATIONS", 16);
  // For each size we will sort 'SAMPLES' different arrays
  private static final int SAMPLES = Integer.getInteger("SAMPLES", 16);

  private static int[][][] setup() {
    System.gc();
    Random R = new Random();
    int[][][] ars = new int[ITERATIONS][][];
    for (int i = 0; i < ITERATIONS; i++) {
      ars[i] = new int[SAMPLES][];
      for (int j = 0; j < SAMPLES; j++) {
        ars[i][j] = new int[BASE_SIZE * (i + 1)];
        for (int k = 0; k < ars[i][j].length; k++) {
          ars[i][j][k] = R.nextInt();
        }
      }
    }
    return ars;
  }

  public static void measure(boolean parallel) {
    int[][][] ars = setup();
    DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("de"));
    for (int i = 0; i < ars.length; i++) {
      long start = System.nanoTime();
      for (int j = 0; j < ars[i].length; j++) {
        if (parallel) {
          quicksort_p(ars[i][j]);
        }
        else {
          quicksort(ars[i][j]);
        }
      }
      long end = System.nanoTime();
      // Print array size and average sorting time for arrays of that size in milliseconds
      System.out.println(df.format(ars[i][0].length) + " " + ((end - start) / ars[i].length) / 1_000_000);
    }

  }

  public static void main(String[] args) {
    test();
    warmup();
    boolean parallel = Boolean.getBoolean("PARALLEL");
    if (parallel) {
      fjp = new ForkJoinPool(Integer.getInteger("PARALLELISM", Runtime.getRuntime().availableProcessors()));
      System.out.println("# Using ForkJoinPool of size: " + fjp.getParallelism());
    }
    System.out.println(parallel ? ("\"" + fjp.getParallelism() + " Threads\"") : "Serial"); // Used as 'columnhead' in gnuplot
    measure(parallel);
  }
}
