package jmh.barker;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import static org.openjdk.jmh.annotations.Scope.Benchmark;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@Threads(3)
@State(Benchmark)
public class CipherReinitBench
{
    static Key key = loadKey();
    static Key loadKey()
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(new File("/opt/dev/cassandra/test/conf/cassandra.keystore"));
            KeyStore store = KeyStore.getInstance("JCEKS");
            store.load(inputStream, "cassandra".toCharArray());
            return store.getKey("testing:1", "cassandra".toCharArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException("failed to read key", e);
        }
    }

    static byte[] data = bytes(32 * 1024);
    static byte[] iv = bytes(16);
    static byte[] iv2 = bytes(16);
    static byte[] bytes(int size)
    {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    static int round;

    static Cipher cipher;
    static
    {
        try
        {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        }
        catch (Exception e)
        {
            throw new RuntimeException("failed to init cipher", e);
        }
    }

    @Benchmark
    public void reuseIV()
    {
        try
        {
            cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

//    @Benchmark
//    public void reinit()
//    {
//        try
//        {
//            byte[] currentIv = round++ % 2 == 0 ? iv : iv2;
//            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(currentIv));
//            cipher.doFinal(data);
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//    }

    @Benchmark
    public void newInstance()
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] currentIv = round++ % 2 == 0 ? iv : iv2;
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(currentIv));
            cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
