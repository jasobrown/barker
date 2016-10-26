package jmh.barker;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
public class ByteBufferRead
{
    private static final int SIZE = 1 << 12;
    private static final int POSITION_READ_OFFSET = 1 << 10;

    private final byte[] array = new byte[SIZE];
    private final ByteBuffer byteBufferHeap = ByteBuffer.allocate(SIZE);
    private final ByteBuffer byteBufferOffHeap = ByteBuffer.allocateDirect(SIZE);
    private final ByteBuf byteBufHeap = Unpooled.buffer(SIZE, SIZE);
    private final ByteBuf byteBufDirect = Unpooled.directBuffer(SIZE, SIZE);

    @Setup
    public void setup()
    {
        byteBufferHeap.limit(SIZE).position(POSITION_READ_OFFSET);
        byteBufferOffHeap.limit(SIZE).position(POSITION_READ_OFFSET);

        byteBufHeap.writerIndex(SIZE).readerIndex(POSITION_READ_OFFSET);
        byteBufDirect.writerIndex(SIZE).readerIndex(POSITION_READ_OFFSET);
    }

    @Benchmark
    public int arrayRead()
    {
        byte[] b = new byte[4];
        System.arraycopy(array, POSITION_READ_OFFSET, b, 0, 4);
        return b[0];
    }

    @Benchmark
    public ByteBuffer byteBufferHeapRead()
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        byteBufferHeap.position(POSITION_READ_OFFSET);
        byteBufferHeap.put(buf);
        return buf;
    }

    @Benchmark
    public ByteBuffer byteBufferOffHeapRead()
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(4);
        byteBufferOffHeap.position(POSITION_READ_OFFSET);
        byteBufferOffHeap.put(buf);
        return buf;
    }

    @Benchmark
    public ByteBuf byteBufHeapRead()
    {
        ByteBuf buf = Unpooled.buffer(4, 4);
        byteBufHeap.readerIndex(POSITION_READ_OFFSET);
        byteBufHeap.readBytes(buf);
        return buf;
    }

    @Benchmark
    public ByteBuf byteBufDirectRead()
    {
        ByteBuf buf = Unpooled.directBuffer(4, 4);
        byteBufDirect.readerIndex(POSITION_READ_OFFSET);
        byteBufDirect.readBytes(buf);
        return buf;
    }
}
