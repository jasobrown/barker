package jmh.barker.stream;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

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
public class StreamingLove
{
    private static final int READ_OFFSET = (1 << 10) + 17;
    private static final int READ_BUF_LENGTH = 15;
    private static final int SRC_BUF_LENGTH = 1 << 12;

    private static final byte[] buf = new byte[SRC_BUF_LENGTH];

    private static CompressedInputStream cis = new CompressedInputStream(buf);
    private static AppendingByteArrayInputStream abais = new AppendingByteArrayInputStream(buf);

//    @Benchmark
//    public int readByte_CIS() throws IOException
//    {
//        cis.current = READ_OFFSET;
//        return cis.read();
//    }
//
//    @Benchmark
//    public byte[] readBuf_CIS() throws IOException
//    {
//        cis.current = READ_OFFSET;
//        byte[] b = new byte[READ_BUF_LENGTH];
//        cis.read(b, 0, READ_BUF_LENGTH);
//        return b;
//    }

    @Benchmark
//    public void readBuf_AcrossBuffers_CIS() throws IOException
    public byte[] readBuf_AcrossBuffers_CIS() throws IOException
    {
//        for (int i = 0; i < 10; i++)
//        {
            cis.dataBuffer.offer(buf);
            cis.current = SRC_BUF_LENGTH - (READ_BUF_LENGTH / 2);
            cis.bufferOffset = 0;
            byte[] b = new byte[READ_BUF_LENGTH];
            cis.read(b, 0, READ_BUF_LENGTH);
//        }
        return b;
    }

//    @Benchmark
//    public int readByte_ABAIS() throws IOException
//    {
//        abais.currentPosition = READ_OFFSET;
//        return abais.read();
//    }
//
//    @Benchmark
//    public byte[] readBuf_ABAIS() throws IOException
//    {
//        abais.currentPosition = READ_OFFSET;
//        byte[] b = new byte[READ_BUF_LENGTH];
//        abais.read(b, 0, READ_BUF_LENGTH);
//        return b;
//    }

    @Benchmark
    public byte[] readBuf_AcrossBuffers_ABAIS() throws IOException
    {
        abais.append(buf);
        abais.currentPosition = SRC_BUF_LENGTH - (READ_BUF_LENGTH / 2);
        byte[] b = new byte[READ_BUF_LENGTH];
        abais.read(b, 0, READ_BUF_LENGTH);
        return b;
    }
}
