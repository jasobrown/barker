package jmh.barker;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * A benchmark to measure the time to create a new IV using SecureRandom
 */
@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class SecureRandomBench
{
    private SecureRandom secureRandom;
    private Random random;

    @Param({ "16", "128", "1024", "4096"})
    private int len;

    @Setup
    public void setup() throws NoSuchAlgorithmException
    {
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        random = new Random();
    }

    @Benchmark
    public byte[] newPlainIV()
    {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    @Benchmark
    public byte[] newSecureIV()
    {
        byte[] b = new byte[len];
        secureRandom.nextBytes(b);
        return b;
    }
}
