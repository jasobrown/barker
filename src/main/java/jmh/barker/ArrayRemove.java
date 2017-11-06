package jmh.barker;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Based on an idea from Cliff Click (http://cliffc.org/blog/2017/11/05/modern-hardware-performance-cache-lines/).
 * Basically, if you don't care about ordering, when removing an item from an array, it's quicker to move
 * the last element of the array into the removed item's slot rather than doing the system.arrayCopy
 * to shift the element down.
 */
@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 8, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ArrayRemove
{
    private static final int length = 1 << 11;
    private static final int removePosition = 321;
    private static final int last = length - 1;
    private final int[] array = new int[length];

    final static ArrayList<Object> arrayList = new ArrayList<>(length);
    static
    {
        for (int i = 0; i < length; i++)
        {
            arrayList.add(new Integer(i));
        }
    }

    @Benchmark
    public void arrayRemove(Blackhole bh)
    {
        System.arraycopy(array, removePosition + 1, array, removePosition, length - removePosition - 1);
        bh.consume(array);
    }

    @Benchmark
    public void arrayCopy(Blackhole bh)
    {
        array[removePosition] = array[last];
        array[last] = 0;
        bh.consume(array);
    }

    @Benchmark
    public void arrayListRemove(Blackhole bh)
    {
        final ArrayList<Object> list = new ArrayList<>(arrayList);
        Object o = list.remove(removePosition);
        bh.consume(o);
    }

    @Benchmark
    public void arrayListCopy(Blackhole bh)
    {
        final ArrayList<Object> list = new ArrayList<>(arrayList);
        list.set(removePosition, list.get(last));
        Object o = list.set(last, null);
        bh.consume(o);
    }
}
