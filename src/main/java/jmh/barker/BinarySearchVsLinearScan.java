package jmh.barker;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 4, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class BinarySearchVsLinearScan
{
    @Param({ "8", "64"})
    private int len;

    private long[] longs;
    private int lowVal;
    private int midVal;
    private int highVal;

    @Setup
    public void setup()
    {
        longs = new long[len];
        for (int i = 0; i < len; i++)
            longs[i] = i;

        lowVal = 3;
        midVal = len / 2;
        highVal = len - 1;
    }

//    @Benchmark
//    public long lowIndex_LinearScan()
//    {
//        long expectedVal = longs[lowVal];
//        for (int i = 0; i < longs.length; i++)
//        {
//            if (longs[i] == expectedVal)
//                return i;
//        }
//        return -1;
//    }
//
//    @Benchmark
//    public long lowIndex_BinarySearch()
//    {
//        long expectedVal = longs[lowVal];
//        return Arrays.binarySearch(longs, expectedVal);
//    }
//
//    @Benchmark
//    public long midIndex_LinearScan()
//    {
//        long expectedVal = longs[midVal];
//        for (int i = 0; i < longs.length; i++)
//        {
//            if (longs[i] == expectedVal)
//                return i;
//        }
//        return -1;
//    }
//
//    @Benchmark
//    public long midIndex_BinarySearch()
//    {
//        long expectedVal = longs[midVal];
//        return Arrays.binarySearch(longs, expectedVal);
//    }

    @Benchmark
    public long highIndex_LinearScan()
    {
        // we can check if the expectedVal is greater than the mid value (assuming values are ordered!),
        // and if it is higher than the mid, we can walk the array in reverse

        long expectedVal = longs[highVal];
        for (int i = longs.length - 1; i >= 0; i--)
        {
            if (longs[i] == expectedVal)
                return i;
        }
        return -1;
    }

//    @Benchmark
//    public long highIndex_BinarySearch()
//    {
//        long expectedVal = longs[highVal];
//        return Arrays.binarySearch(longs, expectedVal);
//    }
}
