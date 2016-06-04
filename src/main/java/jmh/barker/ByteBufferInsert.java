package jmh.barker;

import java.nio.ByteBuffer;
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

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ByteBufferInsert
{
    ByteBuffer buffer = ByteBuffer.allocate(8);

    @Benchmark
    public long byteByByte()
    {
        buffer.put(0, (byte)1);
        buffer.put(1, (byte)1);
        buffer.put(2, (byte)1);
        buffer.put(3, (byte)1);
        buffer.put(4, (byte)1);
        buffer.put(5, (byte)1);
        buffer.put(6, (byte)1);
        buffer.put(7, (byte)1);

        return buffer.getLong(0);
    }

    @Benchmark
    public long byLong()
    {
        buffer.putLong(0, 123423423424L);
        return buffer.getLong(0);
    }
}
