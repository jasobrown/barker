package jmh.barker;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 8, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class GenerateRandomBench
{
    private final Random random = new Random();

    // intentionally not powers-of-2
    @Param({ "17", "53", "129", "277"})
    private int len;

    @Benchmark
    public String oneRandomCall()
    {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return new String(b);
    }

    @Benchmark
    public String looping()
    {
        StringBuilder builder = new StringBuilder();
        while (builder.length()<len)
        {
            builder.append(Long.toHexString(random.nextLong()));
        }
        return builder.toString().substring(0,len);
    }
}
