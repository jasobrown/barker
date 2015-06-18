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
 * Check if the branching caused by a short circuit operator causes performance to degrade
 * due to pipeline stalling / branch misprediction.
 */
@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 16, time = 4, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ShortCircuitOperator
{
    @Benchmark
    public boolean simpleCompare_WithinRange_LogicalAnd()
    {
        char c = 'j';
        return c >= 'd' & c <= 't' ? true : false;
    }

    @Benchmark
    public boolean simpleCompare_WithinRange_ShortCircuitAnd()
    {
        char c = 'j';
        return c >= 'd' && c <= 't' ? true : false;
    }

    @Benchmark
    public boolean simpleCompare_AboveRange_LogicalAnd()
    {
        char c = 'z';
        return c >= 'd' & c <= 't' ? true : false;
    }

    @Benchmark
    public boolean simpleCompare_AboveRange_ShortCircuitAnd()
    {
        char c = 'z';
        return c >= 'd' && c <= 't' ? true : false;
    }

    @Benchmark
    public boolean simpleCompare_BelowRange_LogicalAnd()
    {
        char c = 'a';
        return c >= 'd' & c <= 't' ? true : false;
    }

    @Benchmark
    public boolean simpleCompare_BelowRange_ShortCircuitAnd()
    {
        char c = 'a';
        return c >= 'd' && c <= 't' ? true : false;
    }
}
