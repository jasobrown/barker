package jmh.barker;

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

/**
 * benchmark, in java, of Chris Westin's for loop optimization in c:
 * https://www.bookofbrilliantthings.com/blog/revisiting-some-old-for-loop-lore
 */
@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class CWestinForLoop
{
    private static final int MAX = 100000000;
    private static final int MAX_BUF = 1 << 20;
    private final byte[] buf;

    public CWestinForLoop()
    {
        buf = new byte[MAX_BUF];
        for (int i = 0; i < MAX_BUF; i++)
            buf[i] = (byte)i;
    }

    @Benchmark
    public int standardLoop()
    {
        int x = 0;
        for (int i = 0; i < MAX; i++)
            x = i;
        return x;
    }

    @Benchmark
    public int westinLoop()
    {
        int x = 0;
        for (int i = MAX; i > 0; --i)
            x = i;
        return x;
    }

    @Benchmark
    public int standardArrayLoop()
    {
        int x = 0;
        for (int i = 0; i < MAX_BUF; i++)
            x = buf[i];
        return x;
    }

    @Benchmark
    public int westinArrayLoop()
    {
        int x = 0;
        for (int i = MAX_BUF - 1; i > 0; --i)
            x = buf[i];
        return x;
    }
}
