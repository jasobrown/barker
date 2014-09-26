package jmh.barker;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
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

/**
 * compare checksum speeds.
 *
 * First version of this benchmark updated the byte buffer with a new data on each execution.
 * This was overwhelmingly dominating the benchmarking, so I switched to reusing a buffer.
 */
@State(Scope.Benchmark)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ChecksumBench
{
    private static final String Adler = "Adler";
    private static final String XXHashSafe = "XXHashSafe";
    private static final String XXHashUnsafe = "XXHashUnsafe";
    private static final String XXHashNative = "XXHashNative";

    @Param({"16", "1028", "65336"})
    private int len;
    byte[] buffer;

    @Param({Adler, XXHashSafe, XXHashUnsafe, XXHashNative})
    public String checksumType;
    private Checksum checksum;

    @Setup
    public void setup()
    {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        buffer = new byte[len];
        random.nextBytes(buffer);

        if (Adler.equals(checksumType))
            checksum = new Adler32();
        else if (XXHashSafe.equals(checksumType))
            checksum = new XXHashChecksum(XXHashFactory.safeInstance().hash32());
        else if (XXHashUnsafe.equals(checksumType))
            checksum = new XXHashChecksum(XXHashFactory.unsafeInstance().hash32());
        else if (XXHashNative.equals(checksumType))
            checksum = new XXHashChecksum(XXHashFactory.nativeInstance().hash32());
        else
            throw new IllegalArgumentException("unknown checksum type: " + checksumType);
    }

    @Benchmark
    public long reuseBuffer()
    {
        checksum.reset();
        checksum.update(buffer, 0, len);
        return checksum.getValue();
    }

    @Benchmark
    public long randomBuffer()
    {
        checksum.reset();
        ThreadLocalRandom.current().nextBytes(buffer);
        checksum.update(buffer, 0, len);
        return checksum.getValue();
    }

    public static class XXHashChecksum implements Checksum
    {
        private final XXHash32 xxHash;

        public XXHashChecksum(XXHash32 xxHash)
        {
            this.xxHash = xxHash;
        }

        /**
         * used when {@code update(int)} is called
         */
        private byte[] oneByteArray = new byte[1];

        /**
         * result of the last calculated hash.
         */
        private int curHash;

        public void update(int b)
        {
            oneByteArray[0] = (byte)b;
            update(oneByteArray, 0, 1);
        }

        public void update(byte[] b, int off, int len)
        {
            curHash = xxHash.hash(b, off, len, 0);
        }

        public long getValue()
        {
            return curHash;
        }

        public void reset()
        {
            //nop
        }
    }


}
