package jmh.barker;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class LinkedBlockingQueueBench
{
    @Param({ "16", "128", "1024", "4096"})
    private int len;

    BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
    List<Integer> sink = new ArrayList<>();
    Queue<Integer> clq = new ConcurrentLinkedQueue<>();

    @Setup
    public void setup()
    {
        queue.clear();
        for (int i = 0; i < len; i++)
        {
            queue.add(i);
            clq.add(i);
        }

        sink = new ArrayList<>(len);
    }

    @Benchmark
    public int readOneByOne()
    {
        int val = 0;
        while (!queue.isEmpty())
            val = queue.remove();
        return val;
    }

    @Benchmark
    public int drainTo()
    {
        return queue.drainTo(sink, len);
    }

    @Benchmark
    public int clq()
    {
        int val = 0;
        while (!clq.isEmpty())
            val = clq.remove();
        return val;
    }
}
