package jmh.barker;

import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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
public class NettyWriteByte
{
    static final ByteBuf buf = PooledByteBufAllocator.DEFAULT.ioBuffer(128, 128);
    static final byte[] array = new byte[] {5,18,93,44};

    @Benchmark
    public int write4Bytes()
    {
        buf.writerIndex(0);
        buf.writeByte(1);
        buf.writeByte(71);
        buf.writeByte(23);
        buf.writeByte(2);
        return 4;
    }

    @Benchmark
    public int writeInt()
    {
        buf.writerIndex(0);
        byte[] b = array;
        int i = b[0];
        i |= (b[1] << 8);
        i |= (b[2] << 16);
        i |= (b[3] << 24);
        buf.writeInt(i);
        return 4;
    }

    @Benchmark
    public int writeArray()
    {
        buf.writerIndex(0);
        buf.writeBytes(array);
        return 4;
    }
}
