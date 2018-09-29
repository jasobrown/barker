package jmh.barker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 4, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class AtomicIntegerBench
{

    private final AtomicInteger atomicInteger = new AtomicInteger();
    private volatile int i;
    private final AtomicIntegerFieldUpdater<AtomicIntegerBench> updater = AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerBench.class, "i");

    @Benchmark
    public boolean atomicInteger()
    {
        int cur = atomicInteger.get(); // volatile read
        return atomicInteger.compareAndSet(cur, cur + 1);
    }

    @Benchmark
    public boolean atomicIntegerFieldUpdater()
    {
        int cur = i; // volatile read
        return updater.compareAndSet(this, cur, cur + 1);
    }
}
