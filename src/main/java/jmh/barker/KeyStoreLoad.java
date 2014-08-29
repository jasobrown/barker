package jmh.barker;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.util.FileUtils;

/**
 * A benchmark to measure the time to read a key from a KeyStore vs. reading from a cache
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
public class KeyStoreLoad
{
    // there's a lovely behavior with jceks files that all aliases are lower-cased
    private static final String alias = "AES/GCM/NoPadding:1".toLowerCase();
    private KeyStore store;
    private Map<String, Key> cache;

    @Setup
    public void setup()
    {
        cache = new ConcurrentHashMap<String, Key>();

        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream("/tmp/keystore");
            store = KeyStore.getInstance("jceks");
            store.load(inputStream, "cassandra".toCharArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException("couldn't load keystore", e);
        }
        finally
        {
            FileUtils.safelyClose(inputStream);
        }
    }

    @Benchmark
    public Key readFromKeyStore() throws IOException
    {
        FileInputStream inputStream = null;
        try
        {
            Key key = store.getKey(alias, "cassandra".toCharArray());
            if (key == null)
                throw new IOException(String.format("key %s was not found in keystore", alias));
            return key;
        }
        catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e)
        {
            throw new IOException("failed to load secret key", e);
        }
        finally
        {
            FileUtils.safelyClose(inputStream);
        }
    }

    @Benchmark
    public Key readFromCache() throws IOException
    {
        Key key = cache.get(alias);
        if (key != null)
            return key;
        key = readFromKeyStore();
        cache.put(alias, key);
        return key;
    }

}
