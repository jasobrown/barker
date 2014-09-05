package jmh.barker;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * A benchmark to measure the time to create a new IV using SecureRandom
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class SecureRandomBench
{
    private SecureRandom secureRandom;
    private Random random;

    @Setup
    public void setup() throws NoSuchAlgorithmException
    {
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        random = new Random();
    }

    @Benchmark
    public byte[] newPlainIV()
    {
        byte[] b = new byte[16];
        random.nextBytes(b);
        return b;
    }

    @Benchmark
    public byte[] newSecureIV()
    {
        byte[] b = new byte[16];
        secureRandom.nextBytes(b);
        return b;
    }
}
