package jmh.barker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
public class MultipleInstanceOfChecks
{
    private static final Object i = new Integer(42);

    @Benchmark
    public Object firstCheck()
    {
        if (i instanceof Integer)
            return i;
        return null;
    }

    @Benchmark
    public Object secondCheck()
    {
        if (i instanceof String)
            return "sdfsdf";
        if (i instanceof Integer)
            return i;
        return null;
    }

    @Benchmark
    public Object thridCheck()
    {
        if (i instanceof String)
            return "sdfsdf";
        if (i instanceof ReentrantLock)
            return new CountDownLatch(1);
        if (i instanceof Integer)
            return i;
        return null;
    }
}
